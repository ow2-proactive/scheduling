#!/bin/sh
# $1 awaited working dir

WORKING_DIR=`pwd`

if [ "$1" != "$WORKING_DIR" ]
then
    echo "working dir $WORKING_DIR is not the awaited working dir : $1"
    exit 1
fi
