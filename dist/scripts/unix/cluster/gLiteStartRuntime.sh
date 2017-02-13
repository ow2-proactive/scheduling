#!/bin/sh -x
echo "***********************************************************************"
echo "Running on: $HOSTNAME, as: " `whoami`

echo "copying files from (`echo $PWD`):"
cp *.jar *.class proactive.java.policy proactive-log4j $HOME/

echo "***********************************************************************"
if [ "x$PBS_NODEFILE" != "x" ] ; then
    echo "PBS Nodefile: $PBS_NODEFILE"
    HOST_NODEFILE=$PBS_NODEFILE
fi

if [ "x$LSB_HOSTS" != "x" ] ; then
    echo "LSF Hosts: $LSB_HOSTS"
    HOST_NODEFILE=pwd/lsf_nodefile.$$
    for host in ${LSB_HOSTS}
    do
    echo $host >> ${HOST_NODEFILE}
    done
fi

if [ "x$HOST_NODEFILE" = "x" ]; then
    echo "No hosts File defined. Exiting..."
    exit
fi

echo  "***********************************************************************"
CPU_NEEDED=`cat $HOST_NODEFILE | wc -l`

echo "Node count: $CPU_NEEDED"
echo "Nodes in $HOST_NODEFILE: "
cat $HOST_NODEFILE


echo "***********************************************************************"

CLASSPATH="`echo $@ |  sed -r 's/.*-cp(.*:\.)\ .*/\1/g' | sed -r "s|^ |$HOME\/|g" | sed -r "s|:|:$HOME\/|g"`"
echo "CLASSPATH=$CLASSPATH"

PROACTIVE_COMMAND=`echo $@ | sed -r "s|(.*-cp) .*:\.(\ .*)|\1 $CLASSPATH\2|g" | sed  -r "s|-Djava.security.policy=|-Djava.security.policy=$HOME\/|g" | sed -r "s|-Dlog4j.configuration=file:|-Dlog4j.configuration=file:$HOME\/|g"`
echo "PACMD=$PROACTIVE_COMMAND"

echo  "***********************************************************************"
CPU_NEEDED=`cat $HOST_NODEFILE | wc -l`

#locate java

echo "Checking ssh for each node:"
NODES=`cat $HOST_NODEFILE`
for host in ${NODES}
do
    echo "Launching in $host:"
    ssh -x $host $PROACTIVE_COMMAND &
done
echo "***********************************************************************"
