#!/bin/sh

echo "Process tree killer test : detached commands launcher 2"

nohup /bin/sh PTK_process2.sh &
nohup /bin/sh PTK_process2.sh &
nohup /bin/sh PTK_process2.sh &
nohup /bin/sh PTK_process2.sh &

# wait for 30 seconds but not with sleep!
ping -c 30 127.0.0.1

exit 0