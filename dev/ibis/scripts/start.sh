# !/bin/bash
currentDir=`dirname $0`
#ROOT=/home1/fabrice
#. $ROOT/workProActive/ProActive/modelisation/scripts/common/env.sh

. $currentDir/env.sh

#we initialise the environment using the first
#parameter
$1
shift

#normal
#boot_cluster
#boot 
#echo $JAVA_CMD $@

$JAVA_CMD $@
