#!/bin/sh

echo
echo --- Fractal Helloworld example ---------------------------------------------

workingDir=`dirname $0`
. $workingDir/env.sh

if [ -f $PROACTIVE/lib/ow_deployment_scheduling.jar ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/lib/fractal-gui.jar:$PROACTIVE/lib/fractal-swing.jar:$PROACTIVE/lib/fractal-swing-tmpl.jar:$PROACTIVE/lib/julia-runtime.jar:$PROACTIVE/lib/julia-asm.jar:$PROACTIVE/lib/julia-mixins.jar:$PROACTIVE/lib/SVGGraphics.jar
fi

export CLASSPATH

$JAVACMD org.objectweb.fractal.gui.FractalGUI org.objectweb.proactive.ic2d.gui.components.ProActiveGUI $1 $2 $3



echo
echo ---------------------------------------------------------
