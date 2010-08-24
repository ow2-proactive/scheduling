#!/bin/bash

SUFFIX="/in/"
DIR=$1$SUFFIX

if [ ! -d "$DIR" ]; then
	echo "Could not find working dir: $DIR"
	exit 1
fi

cd $DIR

num=$PAS_TASK_ITERATION
WDIR=$DIR/../tmp.in/$num/

if [ ! -d "$WDIR" ]; then
	mkdir -p $WDIR
fi

convert $DIR/$(($num + 1)).jpg \
	-crop 500x500 \
    +adjoin \
	$WDIR/%d.jpg

exit 0

echo "Could not find files to work on! Job should have ended earlier"
exit 1
