#!/bin/sh

#This launcher launch a native command $1 with arguments specified $@
#after have added directory of the command in $LD_LIBRARY_PATH env var

COMMAND=$1
shift

WORK_DIR=$(dirname $COMMAND)
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$WORK_DIR

if [ ! -e $COMMAND ]
then
echo "can't find native executable to launch, have you built it ? (go to src dir and type : make)"
exit 1
fi

$COMMAND $@
if [ $? == 0 ]
then
echo "execution ok"
exit 0
else
echo  "problem during execution"
exit 1
fi
