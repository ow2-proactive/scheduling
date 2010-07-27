#!/bin/sh
#
# /scripts/pbs/pbsInfrastructure.sh
#
# PBS deployment script for PBSInfrastructure
# Runs a PBS job, requesting one single core,
# that will create a new runtime and register a node
# to a remote Resource Manager
#
# This script is not meant to be run alone, but more like:
#     echo "jobBatchingInfrastructure.sh [params]" | sumitCommand [params]
#

cd $(dirname $0) &>/dev/null

if [ $# -lt 5 ]; then
    echo "Usage: $0 jdk credentials rm nodeName nsName [additional config]"
    exit 0
fi

JAVA=$1
CRED=$2
URL=$3
NAME=$4
NS=$5
CONFIG=""
while [ $# -gt 5 ]; do
    CONFIG="$CONFIG $6"
    shift 1
done

cd $(dirname $0)/../../../ &>/dev/null
PA_SCHEDULER=$(pwd)
cd - &>/dev/null


JARS="$PA_SCHEDULER/dist/lib/ProActive.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/script-js.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/jruby-engine.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/jython-engine.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/commons-logging-1.0.4.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/ProActive.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/ProActive_SRM-common.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/ProActive_ResourceManager.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/ProActive_Scheduler-core.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/ProActive_Scheduler-client.jar"
JARS=$JARS:"$PA_SCHEDULER/dist/lib/ProActive_Scheduler-worker.jar"

$JAVA -cp $JARS \
    -Dpa.scheduler.home=$PA_SCHEDULER \
    -Dpa.rm.home=$PA_SCHEDULER \
	-Djava.security.policy=$PA_SCHEDULER/config/security.java.policy-client \
    $CONFIG \
    org.ow2.proactive.resourcemanager.utils.PAAgentServiceRMStarter \
    -r $URL -n $NAME -s $NS -v $CRED

wait
