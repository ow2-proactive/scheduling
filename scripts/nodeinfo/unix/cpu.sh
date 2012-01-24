#!/bin/bash

total_cores=`grep "processor" /proc/cpuinfo | wc -l`
core_vendor=`grep -m 1 "vendor_id" /proc/cpuinfo`
core_model=`grep -m 1 "model name" /proc/cpuinfo`

IFS=':'
core_vendor=($core_vendor)
core_model=($core_model)

echo "{"
echo \'total_cores\' : \'$total_cores\',
echo \'core_vendor\' : \'${core_vendor[1]}\',
echo \'core_model\' : \'${core_model[1]}\'
echo "}"
