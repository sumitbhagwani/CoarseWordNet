SHELL=/bin/sh

# Top level Makefile for WordNet 2.0 documentation

WN_ROOT = /usr/local/WordNet-2.0
WN_INSTALLDIR = $(WN_ROOT)/doc

SUBDIRS = html pdf ps

install:
	@for dir in $(SUBDIRS) ; \
	do \
	( cd $$dir; echo "Installing $$dir..."; $(MAKE) WN_INSTALLDIR='$(WN_INSTALLDIR)'/$$dir install ) ; \
	done ;
	@echo "Done installind HTML and PDF documentation"

uninstall:
	@for dir in $(SUBDIRS) ; \
	do \
	( cd $$dir; echo "Uninstalling $$dir..."; $(MAKE) WN_INSTALLDIR='$(WN_INSTALLDIR)'/$$dir uninstall ) ; \
	done ;
	@echo "Done uninstalling HTML and PDF documentation"

clean::
	@for dir in $(SUBDIRS) ; \
	 do \
	 ( cd $$dir ; echo "Cleaning subdirectory $$dir" ; $(MAKE) clean ) ; \
	 done

clean::
	@rm -f *~ "#"*
