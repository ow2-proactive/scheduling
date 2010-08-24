#!/bin/bash

if [ $# -lt 1 ]; then
        echo "Usage: $0 imageDir"
        exit 1
fi

num=$PAS_TASK_ITERATION

if [ ! -d "$1/out" ]; then
	mkdir $1/out
fi

montage $1/tmp.out/$num/*jpg \
	-mode concatenate \
	$1/out/$num.jpg

