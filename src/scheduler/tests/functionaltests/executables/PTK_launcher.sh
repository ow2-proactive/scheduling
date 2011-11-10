#!/bin/sh

DIR=`dirname $0`

echo Process tree killer test : detached commands launcher

nohup /bin/sh $DIR/PTK_process.sh &
nohup /bin/sh $DIR/PTK_process.sh &
nohup /bin/sh $DIR/PTK_process.sh &
nohup /bin/sh $DIR/PTK_process.sh
