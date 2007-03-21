#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed ProActive
#

if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../.
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
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/IC2D-old
fi
if [ -f $PROACTIVE/ProActive.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive.jar
fi
if [ -f $PROACTIVE/ProActive_examples.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ProActive_examples.jar
fi
if [ -f $PROACTIVE/lib/log4j.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/log4j.jar
fi
if [ -f $PROACTIVE/lib/xercesImpl.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/xercesImpl.jar
fi
if [ -f $PROACTIVE/lib/bouncycastle.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/bouncycastle.jar
fi
if [ -f $PROACTIVE/lib/components/fractal.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/fractal.jar
fi
if [ -f $PROACTIVE/ic2d.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/ic2d.jar
fi
if [ -f $PROACTIVE/lib/javassist.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/javassist.jar
fi

#--------------------------------------------------
# jar to set when using RMI/SSH
if [ -f $PROACTIVE/lib/jsch.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jsch.jar
fi

#--------------------------------------------------
# jars to set when using Jini
if [ -f $PROACTIVE/lib/jini/jini-core.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jini/jini-core.jar
fi
if [ -f $PROACTIVE/lib/jini/jini-ext.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jini/jini-ext.jar
fi
if [ -f $PROACTIVE/lib/jini/reggie.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/jini/reggie.jar
fi

#--------------------------------------------------
# jars to set when using Globus
if [ -f $PROACTIVE/lib/globus/cog-jglobus-1.2.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/globus/cog-jglobus-1.2.jar
fi
if [ -f $PROACTIVE/lib/globus/cryptix.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/globus/cryptix.jar
fi
if [ -f $PROACTIVE/lib/globus/cryptix32.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/globus/cryptix32.jar
fi
if [ -f $PROACTIVE/lib/globus/cryptix-asn1.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/globus/cryptix-asn1.jar
fi
if [ -f $PROACTIVE/lib/globus/puretls.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/globus/puretls.jar
fi
if [ -f $PROACTIVE/lib/globus/cog-ogce.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/globus/cog-ogce.jar
fi
#--------------------------------------------------
# jar to set when using gLite
if [ -f $PROACTIVE/lib/glite/classad.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/glite/classad.jar
fi
if [ -f $PROACTIVE/lib/glite/glite-wms-jdlj.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/glite/glite-wms-jdlj.jar
fi

#--------------------------------------------------
# jar to set when using Ibis
if [ -f $PROACTIVE/lib/ibis/ibis.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ibis/ibis.jar
fi

#--------------------------------------------------
# jars to set when using Fractal GUI
if [ -f $PROACTIVE/lib/components/asm-2.2.1.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/components/asm-2.2.1.jar
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

#--------------------------------------------------
# jars to set when using Web Services
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
if [ -f $PROACTIVE/lib/ws/xml-apis.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/ws/xml-apis.jar
fi

#--------------------------------------------------------
# jars to set when using Scilab
if [ -f $PROACTIVE/lib/scilab/javasci.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/scilab/javasci.jar
fi 

#--------------------------------------------------------
# when using TimIt
if [ -f $PROACTIVE/lib/timit/batik-awt-util.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/batik-awt-util.jar
fi
if [ -f $PROACTIVE/lib/timit/batik-dom.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/batik-dom.jar
fi
if [ -f $PROACTIVE/lib/timit/batik-svggen.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/batik-svggen.jar
fi
if [ -f $PROACTIVE/lib/timit/batik-util.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/batik-util.jar
fi
if [ -f $PROACTIVE/lib/timit/batik-xml.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/batik-xml.jar
fi
if [ -f $PROACTIVE/lib/timit/commons-cli-1.0.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/commons-cli-1.0.jar
fi
if [ -f $PROACTIVE/lib/timit/jcommon-1.0.6.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/jcommon-1.0.6.jar
fi
if [ -f $PROACTIVE/lib/timit/jdom.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/jdom.jar
fi
if [ -f $PROACTIVE/lib/timit/jfreechart-1.0.2.jar ]
then
	CLASSPATH=$CLASSPATH:$PROACTIVE/lib/timit/jfreechart-1.0.2.jar
fi



#echo "CLASSPATH"=$CLASSPATH 
export CLASSPATH


JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$PROACTIVE/scripts/proactive.java.policy -Dlog4j.configuration=file:$PROACTIVE/scripts/proactive-log4j "

#JAVACMD="$JAVACMD -Dcom.sun.management.jmxremote "
export JAVACMD



