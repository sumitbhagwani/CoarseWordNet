SHELL=/bin/sh

# Makefile for WordNet 2.0 Browser

WN_ROOT = /usr/local/WordNet-2.0
WN_INSTALLDIR = $(WN_ROOT)/bin
WN_INCDIR = $(WN_ROOT)/include
WN_LIBDIR = $(WN_ROOT)/lib

TCL_INCDIR = /usr/local/include
TCL_LIBDIR = /usr/local/lib
TK_INCDIR = /usr/local/include
TK_LIBDIR = /usr/local/lib
X11_INCDIR = /usr/X11/include
X11_LIBDIR = /usr/X11/lib

# The Solaris version of WordNet was built with Tcl/Tk 8.3

WNB_LIBS = -ltk8.3 -ltcl8.3 -lX11 -lm -lsocket -lnsl -ldl -lpthread

CC = gcc
LOCAL_CFLAGS = -O -DUNIX -I$(TCL_INCDIR) -I$(TK_INCDIR) -I$(X11_INCDIR)
CFLAGS = -I$(WN_INCDIR) $(LOCAL_CFLAGS)
LOCAL_LDFLAGS = -L$(TCL_LIBDIR) -L$(TK_LIBDIR) -L$(X11_LIBDIR)
LDFLAGS= -L$(WN_LIBDIR) $(LOCAL_LDFLAGS)
LIBS = -lwn $(WNB_LIBS)

WN_FILES = wishwn wnb
SRC = stubs.c tkAppInit.c
OBJ = stubs.o tkAppInit.o

all: $(WN_FILES)

install: $(WN_FILES)
	@if [ ! -d $(WN_INSTALLDIR) ] ; then \
		echo "Making directory $(WN_INSTALLDIR)" ; \
		mkdir -p $(WN_INSTALLDIR) ; \
		chmod 755 $(WN_INSTALLDIR) ; \
	fi ; 
	@for file in $(WN_FILES) ; \
	 do \
		filename=$(WN_INSTALLDIR)/$$file ; \
		if [ -f $$filename ] ; then \
			echo "Cannot install $$filename: file exists" ; \
		else \
			echo "Installing $$filename" ; \
			cp -p $$file $$filename ; \
			chmod 555 $$filename ; \
		fi ; \
	 done ;
	@echo "Done installing WordNet browser interface in $(WN_INSTALLDIR)"

uninstall:
	@for file in $(WN_FILES) ; \
	 do \
		filename=$(WN_INSTALLDIR)/$$file ; \
		if [ ! -f $$filename ] ; then \
			echo "Cannot uninstall $$filename: not present" ; \
		else \
			echo "Uninstalling $$filename" ; \
			rm -f $$filename ; \
		fi ; \
	 done ;
	@echo "Done uninstalling WordNet browser interface from $(WN_INSTALLDIR)"

wishwn: $(OBJ)
	$(CC) -o wishwn $(LDFLAGS) $(OBJ) $(LIBS)

wnb:

clean:
	@rm -f $(OBJ) wishwn core *~ "#"*

distclean: uninstall clean
