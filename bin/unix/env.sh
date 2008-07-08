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

if [ -d $PA_SCHEDULER/classes ]
then
    CLASSPATH=$CLASSPATH:$PA_SCHEDULER/classes/resource-manager
    CLASSPATH=$CLASSPATH:$PA_SCHEDULER/classes/scheduler
    for i in $PA_SCHEDULER/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
    for i in $PA_SCHEDULER/lib/ProActive/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
    for i in $PA_SCHEDULER/lib/common/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
else 
    for i in $PA_SCHEDULER/dist/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
fi

export CLASSPATH

JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$PA_SCHEDULER/bin/proactive.java.policy -Dlog4j.configuration=file:${PA_SCHEDULER}/bin/proactive-log4j -Dproactive.home=$PA_SCHEDULER -Dpa.scheduler.home=$PA_SCHEDULER -Dpa.rm.home=$PA_SCHEDULER"

echo $JAVACMD

export PA_SCHEDULER
export JAVACMD

