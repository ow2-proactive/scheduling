#! /bin/bash

# Script submited to OAR

PROACTIVE_COMMAND=$1
BOOKED_NODE_ACCESS=$2
HOST_CAPACITY=$3


DEBUG=1

if [ "$DEBUG" != "0" ] ; then
    echo "PROACTIVE_COMMAND:  $PROACTIVE_COMMAND"
    echo "BOOKED_NODE_ACCESS: $BOOKED_NODE_ACCESS"
    echo "HOST_CAPACITY:      $HOST_CAPACITY"
    echo "OAR_JOB_ID:         $OAR_JOB_ID"
fi

TMP_FILE=$(mktemp)

if [ "${HOST_CAPACITY}" == "0" ] ;
then

    # VM Capacity and Host Capacity have not been specified inside GCM Deployment Descriptor
    #
    # Values are automagically computed in this script to start one ProActive Runtime per host and
    # as many Nodes as cores.

    cat $OAR_FILE_NODES | sort | uniq -c > ${TMP_FILE}
    exec 3<> ${TMP_FILE}
    while read CORES HOSTNAME <&3
    do {
        if [ "$DEBUG" != "0" ] ; then
            echo $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND} -c $CORES"
        fi
        $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND} -c $CORES" &
    } done
    exec 3>&-
else
    # Let the user specify HOST Capacity and VM Capacity
    #
    # The full command has been created and put inside PROACTIVE_COMMAND by
    # CommandBuilderProActive

    cat $OAR_FILE_NODES | sort | uniq > ${TMP_FILE}
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
