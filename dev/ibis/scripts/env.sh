#!/bin/bash

##### 

JAVA="/net/home/acontes/tmp/j2sdk1.4.2_04/bin/java  "
#JAVA="/usr/local/ibm-java/IBMJava2-141/bin/java "
pretty_echo() {
  echo "------------ $1 -------------"
  shift
  echo $@ | tr ':' '\n'
  echo "---------------------------"
}

simple_echo() {
  echo "------------ $1 -------------"
  shift
  echo $@ 
  echo "---------------------------"
}

# boot() {
#  BOOTCLASSPATH="/var/tmp/fabrice/Ibis/IOFAb/Ibis/:\
# /var/tmp/fabrice/Ibis/IOFAb/classlibs/jdk:\
# /var/tmp/fabrice/Ibis/IOFAb/ProActive/:\
# /var/tmp/fabrice/Ibis/IOFAb/classlibs/lib:\
# /var/tmp/fabrice/Ibis/IOFAb/jem3D"
#  CLASSPATH="/var/tmp/fabrice/Ibis/IOFAb/ProActive/:\
# /var/tmp/fabrice/Ibis/IOFAb/classlibs/lib:\
# /var/tmp/fabrice/Ibis/IOFAb/jem3D" #$CLASSPATH
# CONFIG=~/workProActive/ProActive/dev/ibis/config/proactiveConfigurationIbis.xml
#  echo "XXXXXXXXXXXXX BOOT MODE XXXXXXXXXXXXXXXXX"
#  pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
#  pretty_echo "CLASSPATH" ""$CLASSPATH
#  pretty_echo "CONFIG" $CONFIG

# JAVA_CMD="$JAVA -Xbootclasspath:$BOOTCLASSPATH
# -Dsun.boot.library.path=$ROOT/workProActive/ProActive/lib:\
# $JAVA_ROOT/jre/lib/i386:$LD_LIBRARY_PATH  \
#  -cp $CLASSPATH \
#  -Dproactive.configuration=$CONFIG " 
# }

boot_cluster() {
 BOOTCLASSPATH="/home1/fabrice/IOFAb/Ibis/:\
/home1/fabrice/IOFAb/jdk:\
/home1/fabrice/IOFAb/ProActive/:\
/home1/fabrice/IOFAb/lib:\
/home1/fabrice/IOFAb/jem3D"

# BOOTCLASSPATH="/home1/fabrice/IOFAb/Ibis/:\
#/home1/fabrice/IOFAb/ProActive/:/home1/fabrice/IOFAb/classlibs/lib:\
#/home1/fabrice/IOFAb/classlibs/jdk "
#
 CLASSPATH="/home1/fabrice/IOFAb/ProActive/:\
/home1/fabrice/IOFAb/Ibis/:\
/home1/fabrice/IOFAb/jem3D/:\
/home1/fabrice/IOFAb/classlibs/lib:" #$CLASSPATH
CONFIG=~/workProActive/ProActive/dev/ibis/config/proactiveConfigurationIbis.xml
 echo "XXXXXXXXXXXXX BOOT CLUSTER MODE XXXXXXXXXXXXXXXXX"
 pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
 pretty_echo "CLASSPATH" ""$CLASSPATH
 pretty_echo "CONFIG" $CONFIG

JAVA_CMD="$JAVA -Xbootclasspath:$BOOTCLASSPATH
-Dibis.library.path=$ROOT/workProActive/ProActive/lib \
-Djava.library.path=$JAVA_ROOT/jre/lib/i386:$LD_LIBRARY_PATH  \
 -cp $CLASSPATH \
 -Dproactive.configuration=$CONFIG "


# JAVA_CMD="$JAVA -Xbootclasspath:$BOOTCLASSPATH
# -Dsun.boot.library.path=$ROOT/workProActive/ProActive/lib:\
# $JAVA_ROOT/jre/lib/i386:$LD_LIBRARY_PATH  \
#  -cp $CLASSPATH \
#  -Dproactive.configuration=$CONFIG "

 echo $JAVA_CMD
 simple_echo "JAVA_CMD" $JAVA_CMD
}


boot_cluster_ibm() {

JAVA="/usr/local/ibm-java/IBMJava2-141/bin/java "
JAVA_ROOT="/usr/local/ibm-java/IBMJava2-141/"
 BOOTCLASSPATH="/home1/fabrice/IOFAb/Ibis/:\
/home1/fabrice/IOFAb/jdk:\
/home1/fabrice/IOFAb/ProActive/:\
/home1/fabrice/IOFAb/lib:\
/home1/fabrice/IOFAb/jem3D"

# BOOTCLASSPATH="/home1/fabrice/IOFAb/Ibis/:\
#/home1/fabrice/IOFAb/ProActive/:/home1/fabrice/IOFAb/classlibs/lib:\
#/home1/fabrice/IOFAb/classlibs/jdk "
#
 CLASSPATH="/home1/fabrice/IOFAb/ProActive/:\
/home1/fabrice/IOFAb/Ibis/:\
/home1/fabrice/IOFAb/jem3D/:\
/home1/fabrice/IOFAb/classlibs/lib:" #$CLASSPATH
CONFIG=~/workProActive/ProActive/dev/ibis/config/proactiveConfigurationIbis.xml
 echo "XXXXXXXXXXXXX BOOT CLUSTER IBM MODE XXXXXXXXXXXXXXXXX"
 pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
 pretty_echo "CLASSPATH" ""$CLASSPATH
 pretty_echo "CONFIG" $CONFIG

JAVA_CMD="$JAVA -Xbootclasspath:$BOOTCLASSPATH
-Dibis.library.path=$ROOT/workProActive/ProActive/lib \
-Djava.library.path=$JAVA_ROOT/jre/lib/i386:$LD_LIBRARY_PATH  \
 -cp $CLASSPATH \
 -Dproactive.configuration=$CONFIG "

 echo $JAVA_CMD
 simple_echo "JAVA_CMD" $JAVA_CMD
}


normal() {
CLASSPATH="$HOME/dev/ProActive/jem3D/bin/:\
$HOME/dev/ProActive/classes:\
/home1/fabrice/workIbis/Ibis/classes:\
$HOME/dev/ProActive/lib/bcel.jar:\
$HOME/dev/ProActive/lib/asm.jar:\
$HOME/dev/ProActive/lib/jini-core.jar:\
$HOME/dev/ProActive/lib/jini-ext.jar:\
$HOME/dev/ProActive/lib/reggie.jar:\
$HOME/dev/ProActive/lib/log4j.jar:\
$HOME/dev/ProActive/lib/xercesImpl.jar:\
$HOME/dev/ProActive/lib/fractal.jar"
BOOTCLASSPATH=""
CONFIG=~/workProActive/ProActive/modelisation/scripts/common/proactiveConfiguration.xml

echo "XXXXXXXXXXXX NORMAL MODE XXXXXXXXXXXXXXXXX"
pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
pretty_echo "CLASSPATH" $CLASSPATH
pretty_echo "CONFIG" $CONFIG
JAVA_CMD="$JAVA -cp $CLASSPATH \
  -Dproactive.configuration=$CONFIG \
   -Djava.library.path=$ROOT/workProActive/ProActive/lib:$LD_LIBRARY_PATH "
}

normal_ibis() {
CLASSPATH="$HOME/dev/ProActive/jem3D/bin/:\
$HOME/dev/ProActive/classes:\
$HOME/dev/ProActive/lib/ibis.jar:\
$HOME/dev/ProActive/lib/bcel.jar:\
$HOME/dev/ProActive/lib/asm.jar:\
$HOME/dev/ProActive/lib/jini-core.jar:\
$HOME/dev/ProActive/lib/jini-ext.jar:\
$HOME/dev/ProActive/lib/reggie.jar:\
$HOME/dev/ProActive/lib/log4j.jar:\
$HOME/dev/ProActive/lib/xercesImpl.jar:\
$HOME/dev/ProActive/lib/bouncycastle.jar:\
$HOME/dev/ProActive/lib/fractal.jar"
BOOTCLASSPATH=""
CONFIG=~/dev/ProActive/dev/ibis/config/proactiveConfigurationIbis.xml

echo "XXXXXXXXXXXX NORMAL MODE XXXXXXXXXXXXXXXXX"
pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
pretty_echo "CLASSPATH" $CLASSPATH
pretty_echo "CONFIG" $CONFIG
JAVA_CMD="$JAVA -cp $CLASSPATH \
  -Dproactive.configuration=$CONFIG \
   -Djava.library.path=$ROOT/dev/ProActive/lib:$LD_LIBRARY_PATH "
}



normal_ibis_debug() {
normal_ibis
JAVA_CMD="$JAVA_CMD -Xdebug -Xnoagent  -Xrunjdwp:transport=dt_socket,address=8000,\
server=y,suspend=n  -Djava.compiler=NONE " 
}



boot_optimizeIt_ibis() {
#boot_cluster
export LD_LIBRARY_PATH=/var/tmp/fabrice/public/OptimizeitSuite60/lib/:$LD_LIBRARY_PATH
echo "XXXXXXXXXXXX OptimizeIt BOOT IBIS MODE XXXXXXXXXXXXXXXXX"
CLASSPATH=$CLASSPATH:/var/tmp/fabrice/public/OptimizeitSuite60/lib/optit.jar 
pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
pretty_echo "CLASSPATH" ""$CLASSPATH
CONFIG=~/workProActive/ProActive/dev/ibis/config/proactiveConfigurationIbis.xml
 BOOTCLASSPATH="/home1/fabrice/IOFAb/Ibis/:\
/home1/fabrice/IOFAb/jdk:\
/home1/fabrice/IOFAb/ProActive/:\
/home1/fabrice/IOFAb/lib:\
/home1/fabrice/IOFAb/jem3D:\
/var/tmp/fabrice/public/OptimizeitSuite60/lib/oibcp.jar"

# JAVA_CMD="$JAVA -Xbootclasspath:$BOOTCLASSPATH
# -Dibis.library.path=$ROOT/workProActive/ProActive/lib \
# -Djava.library.path=$JAVA_ROOT/jre/lib/i386:$LD_LIBRARY_PATH  \
#  -cp $CLASSPATH \
#  -Dproactive.configuration=$CONFIG "

JAVA_CMD="$JAVA -cp $CLASSPATH -Xbootclasspath:$BOOTCLASSPATH \
  -Dibis.library.path=$ROOT/workProActive/ProActive/lib \
  -Dproactive.configuration=$CONFIG \
  -Djava.library.path=$JAVA_ROOT/jre/lib/i386:$ROOT/workProActive/ProActive/lib:$LD_LIBRARY_PATH  \
  -cp $CLASSPATH \
  -Xrunpri:dmp=1  intuitive.audit.Audit "

echo $JAVA_CMD
# JAVA_CMD="$JAVA -cp $CLASSPATH \
#   -Djem3D.node=ibis://fspc060/Test \
#   -Dproactive.configuration=$CONFIG \
#   -Djava.library.path=$ROOT/workProActive/ProActive/lib:$LD_LIBRARY_PATH  \
#   -Xrunpri:dmp=1 -Xbootclasspath/a:/var/tmp/fabrice/public/OptimizeitSuite60/lib/oibcp.jar \
#  intuitive.audit.Audit "
}



optimizeIt_ibis() {
normal_ibis
export LD_LIBRARY_PATH=/var/tmp/fabrice/public/OptimizeitSuite60/lib/:$LD_LIBRARY_PATH
echo "XXXXXXXXXXXX OptimizeIt IBIS MODE XXXXXXXXXXXXXXXXX"
CLASSPATH=$CLASSPATH:/var/tmp/fabrice/public/OptimizeitSuite60/lib/optit.jar 
pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
pretty_echo "CLASSPATH" ""$CLASSPATH
CONFIG=~/workProActive/ProActive/dev/ibis/config/proactiveConfigurationIbis.xml
JAVA_CMD="$JAVA -cp $CLASSPATH \
  -Djem3D.node=ibis://fspc060/Test \
  -Dproactive.configuration=$CONFIG \
  -Djava.library.path=$ROOT/workProActive/ProActive/lib:$LD_LIBRARY_PATH  \
  -Xrunpri:dmp=1 -Xbootclasspath/a:/var/tmp/fabrice/public/OptimizeitSuite60/lib/oibcp.jar \
 intuitive.audit.Audit "
}


optimizeIt() {
normal
export LD_LIBRARY_PATH=/var/tmp/fabrice/public/OptimizeitSuite60/lib/:$LD_LIBRARY_PATH
echo "XXXXXXXXXXXX OptimizeIt MODE XXXXXXXXXXXXXXXXX"
CLASSPATH=$CLASSPATH:/var/tmp/fabrice/public/OptimizeitSuite60/lib/optit.jar 
pretty_echo "BOOTCLASSPATH" $BOOTCLASSPATH
pretty_echo "CLASSPATH" ""$CLASSPATH
CONFIG=~/workProActive/ProActive/dev/ibis/config/proactiveConfiguration.xml
JAVA_CMD="$JAVA -cp $CLASSPATH \
  -Dproactive.configuration=$CONFIG \
  -Djava.library.path=$ROOT/workProActive/ProActive/lib:$LD_LIBRARY_PATH  \
  -Xrunpri:dmp=1 -Xbootclasspath/a:/var/tmp/fabrice/public/OptimizeitSuite60/lib/oibcp.jar \
 intuitive.audit.Audit "
}



##### Variables
 JAVA_HOME=/usr/local/sun-java/jdk1.4/
 ROOT=$HOME
 workingDir=`dirname $0`
 JAVA_ROOT=$JAVA_HOME
 JAVAC=$JAVA_HOME/bin/javac
#IBIS_ROOT=/net/home/fhuet/workIbis/Ibis/src
 IBIS_ROOT=$workingDir/../src
 MANTA_ROOT=/usr/local/VU/manta
 PANDA_ROOT=/home1/rutger/panda
 LFC_ROOT=/usr/local/VU/lfc/lfc-gm
 GM_ROOT=/usr/local/gm/current
 DAS_LIB=/usr/local/VU/daslib/lib/i386_Linux/libdas.a
 PANDA_NETWORK=lfc
 BCEL_ROOT=/0/user/fhuet/OLD/bcel-5.0


#misc proactive libraries
CLASSPATH_LIB=$ROOT/workProActive/ProActive/lib/asm.jar:\
$ROOT/workProActive/ProActive/lib/bcel.jar:\
$ROOT/workProActive/ProActive/lib/bouncycastle.jar:\
$ROOT/workProActive/ProActive/lib/cog.jar:\
$ROOT/workProActive/ProActive/lib/iaik_jce_full.jar:\
$ROOT/workProActive/ProActive/lib/iaik_ssl.jar:\
$ROOT/workProActive/ProActive/lib/javaxCrypto.jar:\
$ROOT/workProActive/ProActive/lib/jini-core.jar:\
$ROOT/workProActive/ProActive/lib/jini-ext.jar:\
$ROOT/workProActive/ProActive/lib/log4j.jar:\
$ROOT/workProActive/ProActive/lib/reggie.jar:\
$ROOT/workProActive/ProActive/lib/xercesImpl.jar:\
$ROOT/workProActive/ProActive/dev/lib/Jama-1.0.1.jar:\
$ROOT/workProActive/ProActive/dev/lib/aelfred-1.2.jar:\
$ROOT/workProActive/ProActive/dev/lib/getopt-1.0.9.jar:\
$ROOT/workProActive/ProActive/dev/lib/jaxp-1.2.jar:\
$ROOT/workProActive/ProActive/dev/lib/jcommon-0.8.7.jar:\
$ROOT/workProActive/ProActive/dev/lib/jdom-1.0b8.jar:\
$ROOT/workProActive/ProActive/dev/lib/jfreechart-0.9.12.jar:\
$ROOT/workProActive/ProActive/dev/lib/oro-2.0.6.jar:\
$ROOT/workProActive/ProActive/dev/lib/oro.jar:\
$ROOT/workProActive/ProActive/dev/lib/sax-2.0.1.jar:\
$ROOT/workProActive/ProActive/dev/lib/xmlParserAPIs.jar

#final CLASSPATH

CLASSPATH="$ROOT/workIbis/Ibis/classes:$CLASSPATH_LIB:\
$ROOT/workProActive/ProActive/classes"


##### decide which mode we want to run

#boot_ceriel
#boot
#normal
#ceriel
#optimizeit
#boot_cluster
#boot_cluster_ceriel
