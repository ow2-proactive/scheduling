#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed ProActive
#

if [ ! $PROACTIVE ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../.
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
CLASSPATH=.
if [ -d $PROACTIVE/classes ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes
fi
if [ -f $PROACTIVE/ProActive.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive.jar
fi
if [ -f $PROACTIVE/lib/bcel.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/bcel.jar
fi
if [ -f $PROACTIVE/lib/asm.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/asm.jar
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
if [ -f $PROACTIVE/lib/cog-jglobus.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/cog-jglobus.jar
fi
if [ -f $PROACTIVE/lib/cryptix.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/cryptix.jar
fi
if [ -f $PROACTIVE/lib/cryptix32.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/cryptix32.jar
fi
if [ -f $PROACTIVE/lib/log4j.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/log4j.jar
fi
if [ -f $PROACTIVE/lib/ibis.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ibis.jar
fi
if [ -f $PROACTIVE/lib/xercesImpl.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/xercesImpl.jar
fi
if [ -f $PROACTIVE/lib/components/fractal.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/fractal.jar
fi
if [ -f $PROACTIVE/lib/cryptix-asn1.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/cryptix-asn1.jar
fi
if [ -f $PROACTIVE/lib/puretls.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/puretls.jar
fi
if [ -f $PROACTIVE/lib/cog-ogce.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/cog-ogce.jar
fi
if [ -f $PROACTIVE/lib/bouncycastle.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/bouncycastle.jar
fi
if [ -f $PROACTIVE/lib/components/fractal-adl.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/fractal-adl.jar
fi
if [ -f $PROACTIVE/lib/components/dtdparser.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/dtdparser.jar
fi
if [ -f $PROACTIVE/lib/components/ow_deployment_scheduling.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/ow_deployment_scheduling.jar
fi
if [ -f $PROACTIVE/lib/components/fractal-gui.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/fractal-gui.jar
fi
if [ -f $PROACTIVE/lib/components/fractal-swing.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/fractal-swing.jar
fi
if [ -f $PROACTIVE/lib/components/julia-runtime.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/julia-runtime.jar
fi
if [ -f $PROACTIVE/lib/components/julia-asm.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/julia-asm.jar
fi
if [ -f $PROACTIVE/lib/components/julia-mixins.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/julia-mixins.jar
fi
if [ -f $PROACTIVE/lib/components/SVGGraphics.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/SVGGraphics.jar
fi
if [ -f $PROACTIVE/lib/jsch.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jsch.jar
fi

if [ -f $PROACTIVE/lib/soap.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/soap.jar
fi
if [ -f $PROACTIVE/lib/wsdl4j.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/wsdl4j.jar
fi

if [ -f $PROACTIVE/lib/axis.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/axis.jar
fi
if [ -f $PROACTIVE/lib/jaxrpc.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jaxrpc.jar
fi
if [ -f $PROACTIVE/lib/activation.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/activation.jar
fi
if [ -f $PROACTIVE/lib/saaj-api.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/saaj-api.jar
fi 

if [ -f $PROACTIVE/lib/commons-logging.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/commons-logging.jar
fi
if [ -f $PROACTIVE/lib/commons-discovery.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/commons-discovery.jar
fi
if [ -f $PROACTIVE/lib/mail.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/mail.jar
fi

echo "CLASSPATH"=$CLASSPATH
export CLASSPATH

JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.library.path=$PROACTIVE/lib -Djava.security.policy=$workingDir/proactive.java.policy -Dlog4j.configuration=$workingDir/proactive-log4j"
export JAVACMD
