#!/bin/sh

workingDir=`dirname $0`

cd $PWD/../../../scripts/unix
. env.sh
cd $workingDir

# TODO: the same on win32
# We do this to avoid the storm when everyody access the NFS.
# Of course the NFS may be down just after the test ...
if echo $@ | grep -q -- -delay ; then
    TOTAL_DELAY=0
    NB_DELAY=0
    while ! head -c1 lib/ProActive.jar >/dev/null || ! [ -d ../logs ]; do
	DELAY=$(($RANDOM % 600))
	sleep $DELAY
	TOTAL_DELAY=$(($TOTAL_DELAY + $DELAY))
	NB_DELAY=$(($NB_DELAY + 1))
    done

    if [ $NB_DELAY -gt 0 ]; then
	echo "Slept $NB_DELAY times => $TOTAL_DELAY seconds waiting for ProActive.jar availability" >&2
    fi
fi

exec nice -19 java -cp $CLASSPATH                               \
                   -Djava.security.manager                      \
                   -Djava.security.policy=proactive.java.policy \
                   -Dlog4j.configuration=proactive-log4j        \
                   org.objectweb.proactive.p2p.daemon.Daemon "$@"
