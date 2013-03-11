# util/dir.mk
# 
# Part of the CCNx distribution.
#
# Copyright (C) 2009-2013 Palo Alto Research Center, Inc.
#
# This work is free software; you can redistribute it and/or modify it under
# the terms of the GNU General Public License version 2 as published by the
# Free Software Foundation.
# This work is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE.
#

SCRIPTSRC = shebang \
	ccndstart.sh ccndstop.sh ccndstatus.sh ccndlogging.sh ccnget.sh ccnput.sh \
	ccntestloop-trampoline \
	ccnd-publish-local-info.sh ccnrpolicyedit.sh

PROGRAMS = ccndstart ccndstop ccndstatus ccntestloop ccndlogging ccnget ccnput \
	 ccnd-publish-local-info ccnrpolicyedit

OTHER_SCRIPTS = ndn-name-dnsifier.py ccnd-autoconfig ndn-install-pubcert \
	ndn-extract-public-key ndn-sign-key

INSTALLED_PROGRAMS = $(PROGRAMS) $(OTHER_SCRIPTS)

default all: $(SCRIPTSRC) $(PROGRAMS)

# ccnd-autoconfig requires bash (uses #!/usr/bin/env bash)
ccndstart ccndstop ccndstatus ccndlogging ccnget ccnput ccnd-publish-local-info: $(SCRIPTSRC) shebang
	./shebang $(SH) $(@:=.sh) > $@
	chmod +x $@

ccntestloop: ccntestloop-trampoline shebang
	./shebang $(SH) ccntestloop-trampoline > $@
	chmod +x $@

clean:
	rm -f $(PROGRAMS) depend

test:
	@echo "Sorry, no util unit tests at this time"
