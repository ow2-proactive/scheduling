#!/bin/bash
for f in `cat $PE_HOSTFILE | cut -f1 -d " "`
do
 echo "---"$f"---"
 ssh $f $* &
done
wait
