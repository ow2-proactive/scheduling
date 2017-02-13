#!/bin/sh
nodelist=`cat  $PBS_NODEFILE`
for i in $nodelist
do
ssh $i $PROACTIVE_COMMAND &
done
wait
