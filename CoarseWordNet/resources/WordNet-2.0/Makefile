SHELL=/bin/sh

# Makefile for both source and binary distribution of WordNet 2.0

#######################################################################
#
# The WordNet Unix release is provided as a binary installation for 
# the following platforms:
#
#	SPARCstation running Solaris 7
#	Silicon Graphics Mips processor running Irix 6.8.3
#	PC running Redhat Linux
#
# If you are using one of these systems, you should select the the
# appropriate PLATFORM below, and use the 'make BinWorld' command to
# install WordNet from the pre-compiled binaries.  Check the settings
# below first to make sure everything is installed where you want it.
#
# This package also includes the source code for the WordNet library
# and interfaces. If you plan on building your own WordNet application,
# or are running on a platform other than those listed above, you
# will have to read the comments below under "Platform specific stuff"
# and make the appropriate selections and changes for your system.
#
#######################################################################

##### Settings for binary installation on Solaris, Irix, or Linux 
##### and source code build.                                     

# To perform a binary installation, make the appropriate settings
# in this section.  You also need to do this if you're going to 
# compile WordNet from source.

# Platform for binary installation.  If you are using one of the 
# following operating systems you do not have to compile the package.

PLATFORM = solaris
#PLATFORM = irix
#PLATFORM = linux

# The WordNet system must be installed in a directory hierarchy.
# WN_ROOT is the root of the WordNet installation hierarchy.
# The default is /usr/local/WordNet-2.0.  If you want to install
# WordNet 2.0 in a different directory, change WN_ROOT below.  
# Note that you must have write permissions for the installation path.

# The installation process will create the following subdirectories:

#	bin	WordNet executable programs
#
# 	dict	WordNet database files (the WNSEARCHDIR environment
#		variable must be set by the user if the database
#		is NOT installed in /usr/local/WordNet-2.0/dict)
#
#	lib	library of WordNet search code functions (libwn.a)
#
#	include	header files for use with library functions
#
#	doc	WordNet Reference Manual in HTML and PDF
#	
# 	man	WordNet Reference Manual in 'man' page format

# Default installation paths

WN_ROOT = /usr/local/WordNet-2.0

# By default, precompiled WordNet binaries are installed in 
# $WN_ROOT/bin.  You may specify another location, such as /usr/local/bin.
# By installing the binaries in /usr/local/bin, you probably do not have to
# change your PATH environment variable.  

WN_BINDIR = $(WN_ROOT)/bin

# By default, the WordNet database files are installed in $WN_ROOT/dict.
# You may specify another location.  If the database is not installed in
# the default location, you MUST set the environment variable 
# WNSEARCHDIR, or the WordNet browser will not be able to run.

WN_DICTDIR = $(WN_ROOT)/dict

# By default, installing the database files is done by copying them
# from the "dict" subdirectory into WN_DICTDIR.  To save space, you
# can set INSTALLCMD to 'mv', which will move the files instead of
# copying them.  This is slightly dangerous since you won't have a
# copy of the database files in the release directory.  If you set
# INSTALLCMD to 'mv', you must set INSTALLFLAGS to nothing.

INSTALLCMD = cp
INSTALLFLAGS = -p
#INSTALLCMD = mv
#INSTALLFLAGS = 

# By default, the WordNet library is installed in $WN_ROOT/lib, and
# the WordNet header files are installed in $WN_ROOT/include.  These
# used when developing new applications that access the WordNet
# database files.

WN_LIBDIR = $(WN_ROOT)/lib
WN_INCDIR = $(WN_ROOT)/include

# By default, WordNet manual pages are installed $WN_ROOT/man. You may 
# specify another location, such as /usr/local/man.  By installing the 
# manual pages in /usr/local/man, you probably do not have to change 
# your MANPATH environment variable.  The WordNet Reference Manual is
# also installed in HTML and PDF formats in $WN_ROOT/doc.

WN_MANDIR = $(WN_ROOT)/man
WN_DOCDIR = $(WN_ROOT)/doc

##### Platform specific stuff (for compiling from source only) 
##### If you are not compiling WordNet 2.0, you do not have to change
##### anything below this line.

# To build WordNet you must use an ANSI C compiler. 'gcc' is freely
# and readily available.  It's what we used to make the precompiled
# binaries.

CC = gcc

# Use this for Solaris
LOCAL_LDFLAGS = -R/usr/openwin/lib:/usr/lib:/usr/local/lib

# Use this for Irix and Linux
#LOCAL_LDFLAGS = 

# "UNIX" must be defined for things to compile...

LOCAL_CFLAGS = -O -DUNIX

# The WordNet browser uses the Tcl/Tk libraries.  WordNet 2.0 should
# work with Tcl/Tk 8.0 and higher.  It was built with version 8.3 on
# Solaris and Linux and 8.0 on Irix.

# If you already have Tcl/Tk 8.0 or higher installed on your system, then
# you can just set the paths below as necessary for your local setup.
# If you do not have Tcl/Tk installed, or do not have the correct
# versions, see the file "README.tcktk" for information about downloading
# and installing Tcl8.3 and Tk8.3 on your system.

# You also need to know where the Xll include files and libraries are
# on your system.

# For the Solaris build, these were used:

TCL_INCDIR = /usr/local/include
TCL_LIBDIR = /usr/local/lib
TK_INCDIR = /usr/local/include
TK_LIBDIR = /usr/local/lib
X11_INCDIR = /usr/openwin/include
X11_LIBDIR = /usr/openwin/lib

# For the Irix build, these were used:

#TCL_INCDIR = /usr/local/include
#TCL_LIBDIR = /usr/local/lib
#TK_INCDIR = /usr/local/include
#TK_LIBDIR = /usr/local/lib
#X11_INCDIR = /usr/include
#X11_LIBDIR = /usr/lib32

# For the Linux build, these were used:

#TCL_INCDIR = /usr/include
#TCL_LIBDIR = /usr/lib
#TK_INCDIR = /usr/include
#TK_LIBDIR = /usr/lib
#X11_INCDIR = /usr/X11R6/include
#X11_LIBDIR = /usr/X11R6/lib

# Libraries needed for Solaris

WNB_LIBS = -ltk8.3 -ltcl8.3 -lX11 -lm -lsocket -lnsl -ldl -lpthread

# Libraries needed for Irix

#WNB_LIBS = -Xlinker -B -Xlinker static -ltk -ltcl -Xlinker -B -Xlinker dynamic -lX11 -lm


# Libraries needed for Linux

#WNB_LIBS = -ltk -ltcl -lX11 -lm -lnsl -ldl -lpthread

###### Finally, the "make" targets ######

all:
	@echo 'First edit Makefile and set "$$PLATFORM" for your computer system'
	@echo "Then specify target: BinWorld, SrcWorld, CleanWorld"

# Target to install WordNet from pre-compiled binaries.  Specify
# $(PLATFORM) above.

BinWorld: wnbin wndict wninclude wnlib wnman wndoc
	@echo "Done installing WordNet 2.0 for $(PLATFORM)..."

# Target to compile and install WordNet from source code

SrcWorld: clean wnsrc wndict wnman wndoc
	@echo "Done creating and installing WordNet 2.0.."

# After the database is installed and everything has been tested,
# use this target to clean up from the make and delete the uninstalled 
# version of the database. 

CleanWorld: clean cleandbfiles
	echo "Done cleaning up and removing source database files"

# You should not need to use any of the followin targets unless
# something goes wrong.

wnbin:
	@echo "Installing WordNet 2.0 binaries in $(WN_BINDIR)..."
	(cd bin/$(PLATFORM); $(MAKE) -k WN_INSTALLDIR='$(WN_BINDIR)' install)

wndict:
	@echo "Installing WordNet 2.0 database in $(WN_DICTDIR)..."
	(cd dict; $(MAKE) -k WN_INSTALLDIR='$(WN_DICTDIR)' INSTALLCMD='$(INSTALLCMD)' INSTALLFLAGS='$(INSTALLFLAGS)' install)

wninclude:
	@echo "Installing WordNet 2.0 header files in $(WN_INCDIR)..."
	(cd include; $(MAKE) -k WN_INSTALLDIR='$(WN_INCDIR)' install)

wnlib:
	@echo "Installing WordNet 2.0 library in $(WN_LIBDIR)..."
	(cd lib/$(PLATFORM); $(MAKE) -k WN_INSTALLDIR='$(WN_LIBDIR)' install)
	(cd lib/wnres; $(MAKE) -k WN_INSTALLDIR='$(WN_LIBDIR)/wnres' install)

wnman:
	@echo "Installing WordNet 2.0 Reference Manaul in $(WN_MANDIR)..."
	(cd man; $(MAKE) -k WN_INSTALLDIR='$(WN_MANDIR)' install)

wndoc:
	@echo "Installing WordNet 2.0 Reference Manual in $(WN_DOCDIR)..."
	(cd doc; $(MAKE) -k WN_INSTALLDIR='$(WN_DOCDIR)' install)

wnsrc:
	(cd src; $(MAKE) -k CC='$(CC)' WN_ROOT='$(WN_ROOT)' TCL_INCDIR='$(TCL_INCDIR)' TK_INCDIR='$(TK_INCDIR)' TCL_LIBDIR='$(TCL_LIBDIR)' TK_LIBDIR='$(TK_LIBDIR)' X11_INCDIR='$(X11_INCDIR)' X11_LIBDIR='$(X11_LIBDIR)' WNB_LIBS='$(WNB_LIBS)' LOCAL_CFLAGS='$(LOCAL_CFLAGS)' LOCAL_LDFLAGS='$(LOCAL_LDFLAGS)' WN_BINDIR='$(WN_BINDIR)' WN_LIBDIR='$(WN_LIBDIR)' WN_INCDIR='$(WN_INCDIR)' all install)

CLEANDIRS = bin dict include lib man doc

clean::
	@rm -f *~ "#"*

clean::
	@for i in $(CLEANDIRS) src ; \
	  do \
		(cd $$i ; echo "Cleaning directory $$i..." ; $(MAKE) clean) ; \
	  done

distclean: clean
	(cd src ; echo "Cleaning directory src..." ; make clean) 

cleandbfiles:
	(cd dict ; $(MAKE) cleandbfiles)

INSTDIRS = bin/$(PLATFORM) dict include lib/$(PLATFORM) lib/wnres man doc

uninstall:
	@for dir in $(INSTDIRS) ; \
	 do \
		( cd $$dir ; echo "Uninstalling subdirectory $$dir..." ; $(MAKE) WN_ROOT='$(WN_ROOT)' uninstall ) \
	 done
