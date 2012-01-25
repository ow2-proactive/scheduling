#!/bin/bash

network=$(
	/sbin/ifconfig | while read line ; do
		echo "," \'$line\' 
	done 
)

network=${network:1}

echo "["
echo ${network[*]}
echo "]"
