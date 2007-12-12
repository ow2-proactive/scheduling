#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed PROACTIVE
#

if [ -z "$PROACTIVE_HOME" ]
then
workingDir=`dirname $0`
PROACTIVE_HOME=$(cd $workingDir/../.././ || (echo "Broken PROACTIVE_HOME installation" ; exit 1) && echo $PWD)
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

if [ -d $PROACTIVE_HOME/classes ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE_HOME/classes/Core
    CLASSPATH=$CLASSPATH:$PROACTIVE_HOME/classes/Extensions
    CLASSPATH=$CLASSPATH:$PROACTIVE_HOME/classes/Extra
    CLASSPATH=$CLASSPATH:$PROACTIVE_HOME/classes/Examples
    for i in $PROACTIVE_HOME/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
else 
    for i in $PROACTIVE_HOME/dist/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
fi

#echo "CLASSPATH"=$CLASSPATH 
export CLASSPATH


JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$PROACTIVE_HOME/scripts/proactive.java.policy -Dlog4j.configuration=file:$PROACTIVE_HOME/scripts/proactive-log4j "

export JAVACMD



