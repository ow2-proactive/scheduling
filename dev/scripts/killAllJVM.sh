#! /bin/sh

for i in $*
do
    echo "Killall JVMS on $i"
    rsh $i killall -9 java
    if [ $? = 0 ]
    then
	echo "rsh $i succefully"
    else
	echo "Could't do rsh in $i"
    fi
done