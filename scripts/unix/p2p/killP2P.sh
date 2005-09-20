#!/bin/sh

. ../env.sh

if [ $# -ne 1 ]; then
   echo 1>&2 Usage: $0 hosts_file
   exit 127
fi

killall rsh

for host in `cat $1`
do
   rsh $host killall -9 java &
   rsh $host killall -9 rmid &
   rsh $host killall -9 rsh &
done
