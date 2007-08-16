 #!/bin/sh

echo
echo --- Scilab example ---------------------------------------------

. scilab_env.sh

workingDir=`dirname $0`
. $workingDir/env.sh
PROACTIVE=$workingDir/../..


$JAVACMD org.objectweb.proactive.extensions.scilab.gui.SciFrame

echo
echo ------------------------------------------------------------
