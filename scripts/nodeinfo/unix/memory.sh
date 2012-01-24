#!/bin/bash

mem_total=`grep "MemTotal" /proc/meminfo`
mem_free=`grep "MemFree" /proc/meminfo`
swap_total=`grep "SwapTotal" /proc/meminfo`
swap_free=`grep "SwapFree" /proc/meminfo`

IFS=':'
mem_total=($mem_total)
mem_free=($mem_free)
swap_total=($swap_total)
swap_free=($swap_free)

echo "{"
echo \'mem_total\' : \'${mem_total[1]}\',
echo \'mem_free\' : \'${mem_free[1]}\',
echo \'swap_total\' : \'${swap_total[1]}\',
echo \'swap_free\' : \'${swap_free[1]}\'
echo "}"
