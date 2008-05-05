#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed PROACTIVE
#

CLASSPATH=.

# User envrionment variable
if [ ! -z "$PROACTIVE_HOME" ] ; then
	PROACTIVE=$PROACTIVE_HOME
fi 


# Internal ProActive scripts can override $PROACTIVE
if [ -z "$PROACTIVE" ]
then
	workingDir=`dirname $0`
	PROACTIVE=$(cd $workingDir/../.././ || (echo "Broken PROACTIVE installation" ; exit 1) && echo $PWD)
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
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Benchmarks
    for i in $PROACTIVE/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
else 
    for i in $PROACTIVE/dist/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
fi

#echo "CLASSPATH"=$CLASSPATH 
export CLASSPATH


JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$PROACTIVE/scripts/proactive.java.policy -Dlog4j.configuration=file:${PROACTIVE}/compile/proactive-log4j -Dproactive.home=$PROACTIVE "

export PROACTIVE
export JAVACMD



