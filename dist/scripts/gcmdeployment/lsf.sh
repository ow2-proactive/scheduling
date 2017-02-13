#! /bin/bash

# Script submited to PBS

PROACTIVE_COMMAND=$1
BOOKED_NODE_ACCESS=$2
HOST_CAPACITY=$3

DEBUG=1

if [ "$DEBUG" != "0" ] ; then
    env | egrep "^LS[FB]" | sort
fi

TMP_FILE=$(mktemp)

if [ "${HOST_CAPACITY}" == "0" ] ;
then

    # VM Capacity and Host Capacity have not been specified inside GCM Deployment Descriptor
    #
    # Values are automagically computed in this script to start one ProActive Runtime per host and
    # as many Nodes as cores.

    echo $LSB_MCPU_HOSTS > ${TMP_FILE}
    declare -a hosts
    read -a hosts < ${TMP_FILE}
    for ((i=0; i<${#hosts[@]}; i += 2)) ;
    do
        HOSTNAME=${hosts[$i]}
        PROCESSORS=${hosts[(($i+1))]}

        if [ "$DEBUG" != "0" ] ; then
                echo $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND} -c ${PROCESSORS}"
        fi
        $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND} -c ${PROCESSORS}" &
    done
else
    # Let the user specify HOST Capacity and VM Capacity
    #
    # The full command has been created and put inside PROACTIVE_COMMAND by
    # CommandBuilderProActive

    echo $LSB_MCPU_HOSTS > ${TMP_FILE}
    declare -a hosts
    read -a hosts < ${TMP_FILE}
    for ((i=0; i<${#hosts[@]}; i += 2)) ;
    do
        HOSTNAME=${hosts[$i]}
        PROCESSORS=${hosts[(($i+1))]}

        if [ "$DEBUG" != "0" ] ; then
                echo $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND}"
        fi
        $BOOKED_NODE_ACCESS -n $HOSTNAME -- "${PROACTIVE_COMMAND}" &
    done
fi

# Kill the job when all Runtime exited
wait

echo "$0 exiting..."
rm -f ${TMP_FILE}
