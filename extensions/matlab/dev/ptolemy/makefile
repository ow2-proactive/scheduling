# Makefile for Java Ptolemy matlab interface
# Based on standard Ptolemy makefiles
#
# @Version: $Id: makefile,v 1.37 2006/09/21 21:58:10 cxh Exp $
# @Author: Zoltan Kemenczy
#
# This directory contains the matlab engine interface
#
# @Copyright (c) 1997-2006 The Regents of the University of California and
# Research in Motion Limited.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
# LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
# OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
# ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
# RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
# SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA  AND RESEARCH IN MOTION LIMITED
# HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################


# Get configuration info
CONFIG =	ptII.mk
include $(CONFIG)


PTOLEMY_LIB_NO_CYGDRIVE = $(PROACTIVE_NO_CYGDRIVE)/dist/lib/ptolemy.jar
PTOLEMY_LIB = $(PROACTIVE)/dist/lib/ptolemy.jar

CLASSPATH =	$(PTOLEMY_LIB_NO_CYGDRIVE)

PTLIB=$(PTJNI_SHAREDLIBRARY_PREFIX)ptmatlab.$(PTJNI_SHAREDLIBRARY_SUFFIX)

all: $(PROACTIVE)/lib/$(MATLAB_VERSION)/$(MATLAB_LIBDIR)/$(PTLIB)



# MAJOR_OS_NAME is set by configure to the major name of the OS
# Under all versions of Windows, MAJOR_OS_NAME = Windows
#PTPACKAGE_NATIVE_JAR =	$(PTPACKAGE)$(MAJOR_OS_NAME).jar
#install: jclass jars $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR)
#	@echo "'$(PTPACKAGE).jar' requires gcc, so we install it in \$$PTII/lib"
#	@echo "for hosts that might not have gcc installed and for Web Start."
#	cp $(PTPACKAGE).jar $(PTPACKAGE_NATIVE_JAR) $(ROOT)/lib
#
#clean: ptmatlab_clean

# Get the rest of the rules
#include $(ROOT)/mk/ptcommon.mk

#--- Specialized part of the makefile: ---

# Under Windows, if cl fails, you may need to set INCLUDE and LIB
# export VCINSTALLDIR="C:/Program Files/Microsoft Visual Studio .NET 2003"
# export MSVCDir=${VCINSTALLDIR}/VC7
# export INCLUDE="${MSVCDir}/ATLMFC/INCLUDE;${MSVCDir}/INCLUDE;${MSVCDir}/PlatformSDK/include/prerelease;${SVCDir}/PlatformSDK/include;C:/Program Files/Microsoft Visual Studio .NET 2003/SDK/v1.1/include;${INCLUDE}"
# export LIB="${MSVCDir}/ATLMFC/LIB;${MSVCDir}/LIB;${MSVCDir}/PlatformSDK/lib/prerelease;${MSVCDir}/PlatformSDK/lib;${FrameworkSDKDir}/lib;${LIB}"

# $(PTMATLAB_cC) is set to cl or gcc by configure in $PTII/mk/ptII.mk
#$(PTLIB): ptmatlab.cc Engine.class
#	CLASSPATH="$(CLASSPATH)" "$(PTJAVA_DIR)/bin/javah" -jni -o ptmatlab.h ptolemy.matlab.Engine
	"$(PTMATLAB_CC)" $(PTMATLAB_CC_FLAGS) \
#		"-I$(MATLAB_DIR)/extern/include" \
#		"-I$(PTJAVA_HOME)/../include" \
#		"-I$(PTJAVA_HOME)/../include/$(PTJNI_ARCHITECTURE)" \
#		$(PTMATLAB_LD_ARGS)

# Rule to build under Microsoft Visual C
#$(PTLIB)_msvc: ptmatlab.cc Engine.class
#	CLASSPATH="$(CLASSPATH)" "$(PTJAVA_DIR)/bin/javah" -jni -o ptmatlab.h ptolemy.matlab.Engine
#	cl \
#		"-I$(MATLAB_DIR)/extern/include" \
#		"-I$(PTJAVA_HOME)/../include" \
#		"-I$(PTJAVA_HOME)/../include/win32" \
#		/Tpptmatlab.cc \
#		-Fe$@ \
#		/link /dll \
#		/libpath:$(MATLAB_DIR)/extern/lib/win32/microsoft/msvc70 \
#		libeng.lib libmx.lib

# Build using gcc
# Note that the Matlab interface does not work under Cygwin with gcc-3.3.3.
#
# $(PTCC) is set to gcc by configure in $PTII/mk/ptII.mk
# -fno-exceptions  - see note at bottom
# $(PTJNI_NO_CYGWIN) is sometimes set to -mno-cygwin and causes
# "Invalid access to memory location" errors


$(PTLIB): ptmatlab.cc $(PTOLEMY_LIB)
	CLASSPATH="$(CLASSPATH)" "$(PTJAVA_DIR)/bin/javah" -jni -o ptmatlab.h ptolemy.matlab.Engine
	$(PTCC) -v -shared $(CFLAGS) $(PTMATLAB_CC_FLAGS) \
		"-I$(MATLAB_DIR_NO_CYGDRIVE)/extern/include" \
		"-I$(PTJAVA_DIR_NO_CYGDRIVE)/include" \
		"-I$(PTJAVA_DIR_NO_CYGDRIVE)/include/$(PTJNI_ARCHITECTURE)" \
		$< \
		-fno-exceptions \
		-o $@ \
		"-L$(MATLAB_DIR_NO_CYGDRIVE)/$(MATLAB_LIBDIR)" "-L$(MATLAB_DIR_NO_CYGDRIVE)/extern/lib/$(PTJNI_ARCHITECTURE)/microsoft" $(PTJNI_SHAREDLIBRARY_LDFLAG) -leng -lmx -lmex


#		"-L$(MATLAB_DIR_NO_CYGDRIVE)/$(MATLAB_LIBDIR)" $(PTJNI_SHAREDLIBRARY_LDFLAG) -leng -lmx
		
$(PROACTIVE)/lib/$(MATLAB_VERSION)/$(MATLAB_LIBDIR)/$(PTLIB) : $(PTLIB)
	mkdir -p $(PROACTIVE)/lib/matsci/$(MATLAB_VERSION)/$(MATLAB_LIBDIR)
	cp $(PTLIB) $(PROACTIVE)/lib/matsci/$(MATLAB_VERSION)/$(MATLAB_LIBDIR)



clean:
	rm -f *.o $(PTLIB)

# Note: -fno-exceptions was added to remove the exception handling frames,
# which for gcc 3.2-3 (prerelease) was introducing an unresolved
# gcc_personality_v0 external, nowhere to be found within the cygwin libraries
# and object files...
