#! /bin/sh

PROACTIVE_COMMAND=$1
BOOKED_NODE_ACCESS=$2
nodes=$(cat $OAR_FILE_NODES | sort | uniq)


#echo "ProActive command: $PROACTIVE_COMMAND"
#echo "Booked node access: $BOOKED_NODE_ACCESS" 
#echo "Nodes: $nodes"

for node in $nodes ; 
do
	echo $BOOKED_NODE_ACCESS $node -- $PROACTIVE_COMMAND
	$BOOKED_NODE_ACCESS $node -- $PROACTIVE_COMMAND &
done

wait