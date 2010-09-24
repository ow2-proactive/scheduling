#!/bin/sh

DIR=`dirname $0`

echo Process tree killer test : detached commands launcher

nohup $DIR/PTK_process.sh &
nohup $DIR/PTK_process.sh &
nohup $DIR/PTK_process.sh &
nohup $DIR/PTK_process.sh
