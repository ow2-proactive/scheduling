#!/bin/bash

if [ $# -lt 1 ]; then
	echo "Usage: $0 imageDir"
	exit 1
fi

OUT=$1/tmp.out/$PAS_TASK_ITERATION

if [ ! -d $OUT ]; then
	mkdir -p $OUT
fi

num=$PAS_TASK_DUPLICATION

IN=$1/tmp.in/$PAS_TASK_ITERATION/$num.jpg

convert $IN \
	-quantize RGB +dither -colors 4 \
	-contrast-stretch 3% \
	$OUT/$PAS_TASK_DUPLICATION.jpg
