#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed PA_SCHEDULER
#

CLASSPATH=.

# ----------------------------------------------------------------------------

JAVA_HOME=${JAVA_HOME-NULL};
if [ "$JAVA_HOME" = "NULL" ]
then
echo
echo "The environment variable JAVA_HOME must be set the current jdk distribution"
echo "installed on your computer."
echo "Use "
echo "    export JAVA_HOME=<the directory where is the JDK>"
exit 127
fi

#set Scheduler install directory env var

PA_SCHEDULER=$(cd $workingDir/../.././ || (echo "Broken Scheduler/Resource Manager installation" ; exit 1) && echo $PWD)

# ----
# Set up the classpath using classes dir or jar files
#
 # Check if classes exists and is not empty
if [ -d $PA_SCHEDULER/classes/scheduler ]
then
    CLASSPATH=$CLASSPATH:$PA_SCHEDULER/classes/common
    CLASSPATH=$CLASSPATH:$PA_SCHEDULER/classes/resource-manager
    CLASSPATH=$CLASSPATH:$PA_SCHEDULER/classes/scheduler
    for i in $PA_SCHEDULER/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
# Use jar index in proactive to point on other lib required by ProActive.jar
	CLASSPATH=$CLASSPATH:$PA_SCHEDULER/lib/ProActive/ProActive.jar
    for i in $PA_SCHEDULER/lib/common/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
    for i in $PA_SCHEDULER/lib/common/script/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
    for i in $PA_SCHEDULER/lib/common/script/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
    #hibernate libs
    for i in $PA_SCHEDULER/lib/hibernate/annotation/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
    for i in $PA_SCHEDULER/lib/hibernate/core/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
else 
    for i in $PA_SCHEDULER/dist/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
fi

export CLASSPATH

#log4j file
if [ "$1" = "" ]
then
	LOG4J_FILE=file:${PA_SCHEDULER}/config/log4j/log4j-client
else
	LOG4J_FILE=file:${PA_SCHEDULER}/config/log4j/$1
fi

JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Dproactive.configuration=$PA_SCHEDULER/config/proactive/ProActiveConfiguration.xml -Djava.security.policy=$PA_SCHEDULER/config/scheduler.java.policy -Dlog4j.configuration=$LOG4J_FILE -Dproactive.home=$PA_SCHEDULER -Dpa.scheduler.home=$PA_SCHEDULER -Dpa.rm.home=$PA_SCHEDULER"

#echo $JAVACMD

export PA_SCHEDULER
export JAVACMD

