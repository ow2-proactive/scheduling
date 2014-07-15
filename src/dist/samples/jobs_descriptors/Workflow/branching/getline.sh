#!/bin/bash


it=$PAS_TASK_ITERATION

line=$(cat $1/input |head -n $(($it + 1)) |tail -n 1)

echo $line > $1/line
