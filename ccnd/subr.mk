$(CSRC) $(HSRC) $(SCRIPTSRC):
	test -f $(SRCDIR)/$@ && ln -s $(SRCDIR)/$@

$(DUPDIR):
	test -d $(SRCDIR)/$(DUPDIR) && mkdir $(DUPDIR) && cp -p $(SRCDIR)/$(DUPDIR)/* $(DUPDIR)

$(OBJDIR)/Makefile: Makefile
	test -d $(OBJDIR) || mkdir $(OBJDIR)
	test -f $(OBJDIR)/Makefile && mv $(OBJDIR)/Makefile $(OBJDIR)/Makefile~ ||:
	cp -p Makefile $(OBJDIR)/Makefile

coverage:
	X () { test -f $$1 || return 0; gcov $$*; }; X *.gc??

shared:

depend: Makefile $(CSRC)
	for i in $(CSRC); do gcc -MM $(CPREFLAGS) $$i; done > depend
	tail -n `wc -l < depend` Makefile | diff - depend
