#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed PROACTIVE
#

PROACTIVE=/user/vjuresch/home/workspace/ProActive


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

    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Core
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Extensions
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Extra
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Examples
    CLASSPATH=$CLASSPATH:$PROACTIVE/doc-src/guided_tour/examples/DistributedPrimesMW/bin

    for i in $PROACTIVE/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done




echo "CLASSPATH"=$CLASSPATH 
export CLASSPATH


JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$PROACTIVE/scripts/proactive.java.policy -Dlog4j.configuration=file:$PROACTIVE/scripts/proactive-log4j -Dproactive.home=$PROACTIVE "

export JAVACMD



