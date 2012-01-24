#!/bin/bash

processes=$(
	ps -ef | grep -v UID | while read line ; do
		echo "," \'$line\' 
	done 
)

processes=${processes:1}

echo "["
echo ${processes[*]}
echo "]"
