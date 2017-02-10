#! /bin/bash

# Script submited to PBS

PROACTIVE_COMMAND=$1
BOOKED_NODE_ACCESS=$2
HOST_CAPACITY=$3
PPN=$4

DEBUG=1

if [ "$DEBUG" != "0" ] ; then
    echo "PBS_O_HOST:         $PBS_O_HOST"
    echo "PBS_O_QUEUE:        $PBS_O_QUEUE"
    echo "PBS_O_WORKDIR:      $PBS_O_WORKDIR"
    echo "PBS_ENVIRONMENT:    $PBS_ENVIRONMENT"
    echo "PBS_JOBID:          $PBS_JOBID"
    echo "PBS_JOBNAME:        $PBS_JOBNAME"
    echo "PBS_NODEFILE:       $PBS_NODEFILE"
    echo "PBS_QUEUE:          $PBS_QUEUE"
    echo "PROACTIVE_COMMAND:  $PROACTIVE_COMMAND"
    echo "BOOKED_NODE_ACCESS: $BOOKED_NODE_ACCESS"
    echo "HOST_CAPACITY:      $HOST_CAPACITY"
    echo "PPN:                $PPN"
    env
    echo "------- PBS_NODEFILE  ------"
    cat $PBS_NODEFILE
    echo "----------------------------"

fi

TMP_FILE=$(mktemp)

# If ppn is specified then each host will appear ppn times in PBS_NODEFILE
# If not each host will appear only once
#
# ppn is passed as parameter by GroupPBS since it cannot be gessed from this script
# We cannot distinguish ppn=1 from no ppn specified

if [ "${HOST_CAPACITY}" == "0" ] ;
then

    # VM Capacity and Host Capacity have not been specified inside GCM Deployment Descriptor
    #
    # Values are automagically computed in this script to start one ProActive Runtime per host and
    # as many Nodes as cores.

    cat $PBS_NODEFILE | sort | uniq > ${TMP_FILE}
    exec 3<> ${TMP_FILE}
    while read HOSTNAME <&3
    do {
        if [ "$PPN" == "0" ] ; then
            if [ "$DEBUG" != "0" ] ; then
                echo $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND}"
            fi
            $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND}" &
        else
            if [ "$DEBUG" != "0" ] ; then
                echo $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND} -c ${PPN}"
            fi
            $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND} -c ${PPN}" &
        fi
    } done
    exec 3>&-
else
    # Let the user specify HOST Capacity and VM Capacity
    #
    # The full command has been created and put inside PROACTIVE_COMMAND by
    # CommandBuilderProActive

    cat $PBS_NODEFILE | sort | uniq > ${TMP_FILE}
    exec 3<> ${TMP_FILE}
    while read HOSTNAME <&3
    do {
        if [ "$DEBUG" != "0" ] ; then
            echo $BOOKED_NODE_ACCESS $HOSTNAME -- ${PROACTIVE_COMMAND}
        fi
        $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND}" &
    } done
    exec 3>&-
fi

# Kill the job when all Runtime exited
wait

echo "$0 exiting..."
rm -f ${TMP_FILE}
