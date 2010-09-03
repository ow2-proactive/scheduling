#!/bin/sh

IT=$PAS_TASK_ITERATION
DUP=$PAS_TASK_REPLICATION

echo -n "param it $2:" > $1/native_result_${IT}_${DUP}
echo -n "param dup $3:" >> $1/native_result_${IT}_${DUP}
echo -n "env it ${PAS_TASK_ITERATION}:" >> $1/native_result_${IT}_${DUP}
echo -n "env dup ${PAS_TASK_REPLICATION}" >> $1/native_result_${IT}_${DUP}

exit 0
