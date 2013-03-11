/**
 * @file ndn-pubkey-name.c
 *
 * @brief Command line utility to print out public key name stored in .pubcert file
 *
 * Copyright (c) 2013 University of California, Los Angeles
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.1 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include <ccn/ccn.h>
#include <ccn/uri.h>
#include <stdio.h>

int
main(int argc, char *argv[])
{
  struct ccn *ccn = NULL;
  int res = 0;

  ccn = ccn_create ();
  if (ccn != NULL) {
    struct ccn_charbuf *name;
    struct ccn_signing_params sp = CCN_SIGNING_PARAMS_INIT;

    name = ccn_charbuf_create ();
    res = ccn_get_public_key_and_name (ccn, &sp, NULL, NULL, name);

    if (res >= 0 && name->length > 0) {
      struct ccn_charbuf *uri_name = ccn_charbuf_create ();

      ccn_uri_append (uri_name, name->buf, name->length, 0);
      fprintf (stdout, "%s\n", ccn_charbuf_as_string (uri_name));

      res = 0;
      ccn_charbuf_destroy (&uri_name);
    }
    else {
      fprintf (stderr, "ERROR: cannot load public key or public key name\n");
      res = -2;
    }

    ccn_charbuf_destroy (&name);
  }
  else
    {
      fprintf (stderr, "ERROR: cannot create ccn handle\n");
      res = -1;
    }

  return res;
}
