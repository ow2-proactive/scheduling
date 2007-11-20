#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed ProActive
#

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$(cd $workingDir/../.././ || (echo "Broken ProActive installation" ; exit 1) && echo $PWD)
CLASSPATH=.
fi


# ----------------------------------------------------------------------------

JAVA_HOME=${JAVA_HOME-NULL};
if [ "$JAVA_HOME" = "NULL" ]
then
echo
echo "The enviroment variable JAVA_HOME must be set the current jdk distribution"
echo "installed on your computer."
echo "Use "
echo "    export JAVA_HOME=<the directory where is the JDK>"
exit 127
fi

# ----
# Set up the classpath using classes dir or jar files
#

if [ -d $PROACTIVE/classes ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Core
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Extensions
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Extra
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Examples
    for i in $PROACTIVE/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
else 
    for i in $PROACTIVE/dist/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
fi

if [ -f $PROACTIVE/ProActive_examples.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive_examples.jar
fi
if [ -f $PROACTIVE/ic2d.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ic2d.jar
fi

#echo "CLASSPATH"=$CLASSPATH 
export CLASSPATH


JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$PROACTIVE/scripts/proactive.java.policy -Dlog4j.configuration=file:$PROACTIVE/scripts/proactive-log4j "
#export LD_LIBRARY_PATH=~/softs/yjp/bin/linux-amd64
#JAVACMD="$JAVACMD -agentlib:yjpagent -Dcom.sun.management.jmxremote "
export JAVACMD



