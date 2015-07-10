#!/bin/sh

IT=$2
DUP=$3

echo -n "param it $2:" > $1/native_result_${IT}_${DUP}
echo -n "param dup $3:" >> $1/native_result_${IT}_${DUP}
echo -n "env it ${variables_PA_TASK_ITERATION}:" >> $1/native_result_${IT}_${DUP}
echo -n "env dup ${variables_PA_TASK_REPLICATION}" >> $1/native_result_${IT}_${DUP}

exit 0
