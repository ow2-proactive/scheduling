#! /bin/sh

for i in $*
do
    echo "Start Daemon on $i"
    rsh $i /net/home/adicosta/ProActiveSecure/ProActive/scripts/unix/startP2PService.sh rmi 2410 /net/home/adicosta/ProActiveSecure/ProActive/ServerList3 &
    if [ $? = 0 ]
    then
	echo "rsh $i succefully"
    else
	echo "Could't do rsh in $i"
    fi
done
