#! /bin/sh

for i in $*
do
    echo "Start Daemon on $i"
    rsh $i $PWD/../../scripts/unix/startP2PService.sh rmi 2410 -f $PWD/ServerList &
    if [ $? = 0 ]
    then
	echo "rsh $i successfully"
    else
	echo "Couldn't do rsh in $i"
    fi
done
