/*
 * Part of the NDNx Java Library.
 *
 * Portions Copyright (C) 2013 Regents of the University of California.
 * 
 * Based on the CCNx C Library by PARC.
 * Copyright (C) 2011-2013 Palo Alto Research Center, Inc.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation. 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.ndnx.ndn.profiles.versioning;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import junit.framework.Assert;

import org.ndnx.ndn.NDNHandle;
import org.ndnx.ndn.impl.NDNFlowControl.SaveType;
import org.ndnx.ndn.io.content.NDNStringObject;
import org.ndnx.ndn.profiles.VersioningProfile;
import org.ndnx.ndn.profiles.versioning.VersionNumber;
import org.ndnx.ndn.profiles.versioning.VersioningInterest;
import org.ndnx.ndn.protocol.NDNTime;
import org.ndnx.ndn.protocol.ContentName;
import org.ndnx.ndn.protocol.MalformedContentNameStringException;
import org.ndnx.ndn.profiles.versioning.VersioningHelper.ReceivedData;
import org.ndnx.ndn.profiles.versioning.VersioningHelper.TestListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is really a super-set of the VersioningInterestManager tests.
 * What we need to test here is that we can express interests for
 * multiple names and multiple listeners.  Then it comes down to
 * does VersioningInterestManager work (and that is tested
 * in VersioningInterestManagerTestRepo).
 */
public class VersioningInterestTestRepo {
	protected final Random _rnd = new Random();
	protected final ContentName prefix;
	protected NDNHandle recvhandle = null;
	protected NDNHandle sendhandle = null;
	
	protected final static long TIMEOUT=30000;
	protected final static long SEND_PAUSE = 30;

	public VersioningInterestTestRepo() throws MalformedContentNameStringException {
		prefix  = ContentName.fromNative(String.format("/repotest/test_%016X", _rnd.nextLong()));
	}

	@Before
	public void setUp() throws Exception {
		recvhandle = NDNHandle.open();
		sendhandle = NDNHandle.open();
	}

	@After
	public void tearDown() throws Exception {
		recvhandle.close();
		sendhandle.close();
	}

	// ======================================================
	
	/**
	 * Two basenames going to one listener
	 */
	@Test
	public void testTwoNamesOneListener() throws Exception {
		System.out.println("****** testTwoNamesOneListener starting");
		ContentName base1 = new ContentName(prefix, String.format("content_%016X", _rnd.nextLong()));
		ContentName base2 = new ContentName(prefix, String.format("content_%016X", _rnd.nextLong()));

		ContentName [] names = new ContentName [] {base1, base2};

		
		TestListener listener = new TestListener();
		
		VersioningInterest vi = new VersioningInterest(recvhandle);
		
		vi.expressInterest(base1, listener);
		vi.expressInterest(base2, listener);
		
		// send data
		NDNTime now = NDNTime.now();
		long start = now.getTime();
		long stop = start + 1000000L;
		
		int count = 100;
		TreeSet<NDNTime> sent = sendStreamUniform(sendhandle, names, start, stop, count);
		
		// now make sure we get them all
		boolean b = listener.cl.waitForValue(count, TIMEOUT);
		System.out.println("Received: " + listener.cl.getValue());
		Assert.assertTrue(b);
		
		HashSet<NDNTime> recv = new HashSet<NDNTime>();
		for(ReceivedData data : listener.received) {
			ContentName name = data.object.name();
			NDNTime version = VersioningProfile.getLastVersionAsTimestamp(name);
			recv.add(version);
		}

		// and compare the received versions
		boolean missing = false;
		for(NDNTime version : sent) {
			if( !recv.contains(version) ) {
				System.out.println("recv missing version " + new VersionNumber(version));
				missing = true;
			}
		}
		Assert.assertFalse(missing);
		vi.close();
		System.out.println("****** testTwoNamesOneListener done");
	}
	
	/**
	 * Three basenames, each with own listener, plus one listener that gets them all
	 */
	@Test
	public void testThreeNamesFourListener() throws Exception {
		System.out.println("****** testThreeNamesFourListener starting");
		ContentName base1 = new ContentName(prefix, String.format("content_%016X", _rnd.nextLong()));
		ContentName base2 = new ContentName(prefix, String.format("content_%016X", _rnd.nextLong()));
		ContentName base3 = new ContentName(prefix, String.format("content_%016X", _rnd.nextLong()));

		ContentName [] names = new ContentName [] {base1, base2, base3};

		TestListener listener1 = new TestListener();
		TestListener listener2 = new TestListener();
		TestListener listener3 = new TestListener();
		TestListener listener4 = new TestListener();
		
		VersioningInterest vi = new VersioningInterest(recvhandle);
		
		vi.expressInterest(base1, listener1);
		vi.expressInterest(base2, listener2);
		vi.expressInterest(base3, listener3);
		vi.expressInterest(base1, listener4);
		vi.expressInterest(base2, listener4);
		vi.expressInterest(base3, listener4);
		
		// send data
		NDNTime now = NDNTime.now();
		long start = now.getTime();
		long stop = start + 1000000L;
		
		int count = 100;
		TreeSet<NDNTime> sent = sendStreamUniform(sendhandle, names, start, stop, count);
		
		// now make sure we get them all
		boolean b = listener4.cl.waitForValue(count, TIMEOUT);
		System.out.println("Received: " + listener4.cl.getValue());
		Assert.assertTrue(b);
		
		HashSet<NDNTime> recv = new HashSet<NDNTime>();
		for(ReceivedData data : listener4.received) {
			ContentName name = data.object.name();
			NDNTime version = VersioningProfile.getLastVersionAsTimestamp(name);
			recv.add(version);
		}

		// and compare the received versions
		boolean missing = false;
		for(NDNTime version : sent) {
			if( !recv.contains(version) ) {
				System.out.println("recv missing version " + new VersionNumber(version));
				missing = true;
			}
		}
		Assert.assertFalse(missing);
		
		// Make sure other listeners got their shares
		long sum = listener1.cl.getValue() + listener2.cl.getValue() + listener3.cl.getValue();
		System.out.println("Other listeners got " + sum);
		Assert.assertEquals(count, (int) sum);
		
		vi.close();
		
		System.out.println("****** testThreeNamesFourListener done");
	}
	
	// ========================================================
	
	private TreeSet<NDNTime> sendStreamUniform(NDNHandle handle, ContentName [] names, long start_time, long stop_time, int count) throws Exception {
		TreeSet<NDNTime> sent = new TreeSet<NDNTime>();

		long [] sends = new long[names.length];
		for(int i = 0; i < sends.length; i++ )
			sends[i] = 0;
		
		int width = (int) (stop_time - start_time + 1);
		for(int i = 0; i < count; i++) {
			NDNTime version;

			// pick the name
			int j = _rnd.nextInt(names.length);
			ContentName name = names[j];
			sends[j]++;
			
			// avoid sending duplicate version numbers
			do {
				int delta = _rnd.nextInt(width);
				version = new NDNTime(start_time + delta);
			} while( !sent.add(version));

//			System.out.println("Sending " + i);
			send(handle, name, version);
		}
		
		System.out.println("sends: " + Arrays.toString(sends));
		
		return sent;
	}
	
	private void send(NDNHandle handle, ContentName name, NDNTime version) throws IOException, InterruptedException {
		
		NDNStringObject so = new NDNStringObject(name, "Hello, World " + version, SaveType.LOCALREPOSITORY, handle);
		int maxtry = 10;
		int trycount = 0;
		IOException error = null;
		
		do {
			error = null;
			try {
				// do this every time, not just on errors.  The FlowController seems
				// to not like to receive a lot of objects all at once.
				// With each error, sleep longer
				Thread.sleep(SEND_PAUSE * (2 * trycount + 1));
				
				boolean b = so.save(version);
				if( b ) {
					so.close();
					return;
				} else {
					throw new IOException("Not saved");
				}
			} catch(IOException e) {
				error = e;
				Thread.sleep(SEND_PAUSE * (2 * trycount + 1));
			} 
			
			trycount++;
		} while( trycount < maxtry && null != error );
		
		if( null != error ) {
			throw error;
		}

//		ContentName versionedName = so.getVersionedName();
//		so = new NDNStringObject(name, handle);
		
	}

}
