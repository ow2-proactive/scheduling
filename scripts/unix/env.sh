#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed ProActive
#

workingDir=`dirname $0`
PROACTIVE=$workingDir/../../.

# ----------------------------------------------------------------------------



# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed ProActive
#

PROACTIVE_TEMP=$HOME/proactive-tmp

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
# Try to set proactive-tmp to the right place
# 
# -- try to set proactive-tmp at the same level as ProActive directory
if [ ! -d $PROACTIVE_TEMP ]
then
    PROACTIVE_TEMP=$workingDir/../../../proactive-tmp
fi
# -- try to set proactive-tmp one level above ProActive directory
if [ ! -d $PROACTIVE_TEMP ]
then
    PROACTIVE_TEMP=$workingDir/../../../../proactive-tmp
fi
# -- proactive-tmp not created yet : back up to user directory
if [ ! -d $PROACTIVE_TEMP ]
then
    PROACTIVE_TEMP=$HOME/proactive-tmp
fi

echo "PROACTIVE_TEMP="$PROACTIVE_TEMP

# ----
# Set up the classpath using classes dir or jar files
# 
CLASSPATH=.:$PROACTIVE_TEMP
if [ -d $PROACTIVE/classes ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes
fi
if [ -f $PROACTIVE/ProActive.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive.jar
fi
if [ -f $PROACTIVE/ProActive_examples.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive_examples.jar
fi
if [ -f $PROACTIVE/ic2d.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ic2d.jar
fi
if [ -f $PROACTIVE/lib/jini-core.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jini-core.jar
fi
if [ -f $PROACTIVE/lib/jini-ext.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jini-ext.jar
fi
if [ -f $PROACTIVE/lib/reggie.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/reggie.jar
fi
if [ -f $PROACTIVE/lib/bcel.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/bcel.jar
fi

echo "CLASSPATH"=$CLASSPATH
export CLASSPATH

JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$workingDir/proactive.java.policy"
export JAVACMD






