#!/bin/sh
nodelist=`cat  $PBS_NODEFILE`
for i in $nodelist
do
rsh $i $PROACTIVE_COMMAND &
done
wait
