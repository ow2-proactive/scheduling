#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed ProActive
#




if [ ! $PROACTIVE ]
then
workingDir=`dirname $0`


PROACTIVE=$workingDir/../../../.
export PROACTIVE
fi
. $workingDir/../env.sh
if [ ! $ENV_WS]
then
export ENV_WS=env_ws

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

if [ -f $PROACTIVE/lib/ws/soap.jar ] 
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/soap.jar
fi
if [ -f $PROACTIVE/lib/ws/wsdl4j.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/wsdl4j.jar
fi

if [ -f $PROACTIVE/lib/ws/axis.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/axis.jar
fi
if [ -f $PROACTIVE/lib/ws/jaxrpc.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/jaxrpc.jar
fi
if [ -f $PROACTIVE/lib/ws/activation.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/activation.jar
fi
if [ -f $PROACTIVE/lib/ws/saaj-api.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/saaj-api.jar
fi 

if [ -f $PROACTIVE/lib/ws/commons-logging.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/commons-logging.jar
fi
if [ -f $PROACTIVE/lib/ws/commons-discovery.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/commons-discovery.jar
fi
if [ -f $PROACTIVE/lib/ws/mail.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/mail.jar
fi
if [ -f $PROACTIVE/dev/lib/xml-apis.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/dev/lib/xml-apis.jar
fi
fi
echo CLASSPATHWS=$CLASSPATH

