#!/bin/sh

echo "Process tree killer test : detached commands launcher"

nohup /bin/sh PTK_process.sh &
nohup /bin/sh PTK_process.sh &
nohup /bin/sh PTK_process.sh &
nohup /bin/sh PTK_process.sh &

# wait for 10 seconds but not with sleep!
ping -c 10 127.0.0.1

exit 0

