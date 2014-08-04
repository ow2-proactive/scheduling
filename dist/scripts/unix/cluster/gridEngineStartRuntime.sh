#!/bin/bash
for f in `awk '{ for (i=0;i<$2;++i) {print $1} }' $PE_HOSTFILE`
do
 echo "---"$f"---"
 ssh $f "$@" &
done
wait
