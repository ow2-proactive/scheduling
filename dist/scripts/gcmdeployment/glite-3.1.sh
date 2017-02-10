#!/bin/sh
# ----------------------------------------------------------------------------
#
# This variable should be set to the directory where is installed PROACTIVE
#

CLASSPATH=.

JOBTYPE=$1
export JAVA_HOME=$2
export PROACTIVE=$3

shift 3

# ----
# Set up the classpath using classes dir or jar files
#

if [ -d $PROACTIVE/classes ]
then
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Core
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Extensions
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Extra
    CLASSPATH=$CLASSPATH:$PROACTIVE/classes/Examples
    for i in $PROACTIVE/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
else
    for i in $PROACTIVE/dist/lib/*.jar ; do
      CLASSPATH=$CLASSPATH:$i
    done
fi

#echo "CLASSPATH"=$CLASSPATH
export CLASSPATH


if [ $JOBTYPE = "parallel" -o $JOBTYPE = "Parallel" ]
then
    echo "Running on: $HOSTNAME, as: " `whoami`
    echo "***********************************************************************"
    if [ "x$PBS_NODEFILE" != "x" ] ; then
        echo "PBS Nodefile: $PBS_NODEFILE"
        HOST_NODEFILE=$PBS_NODEFILE
    fi

    if [ "x$LSB_HOSTS" != "x" ] ; then
        echo "LSF Hosts: $LSB_HOSTS"
        HOST_NODEFILE="lsf_nodefile.$$"
        for host in ${LSB_HOSTS}
        do
            echo $host >> ${HOST_NODEFILE}
        done
    fi

    if [ "x$HOST_NODEFILE" = "x" ]; then
        echo "No hosts File defined. Exiting..."
        exit
    fi
    echo "***********************************************************************"
    CPU_NEEDED=`cat $HOST_NODEFILE | wc -l`

    echo "Node count: $CPU_NEEDED"
    echo "Nodes in $HOST_NODEFILE: "
    cat $HOST_NODEFILE
    echo "***********************************************************************"

    for host in `cat $HOST_NODEFILE`
    do
        echo "Launching in $host:"
        ssh -x $host /bin/hostname
        #ssh -x $host $PROACTIVE_COMMAND &
    done
    echo "***********************************************************************"

else
    JAVACMD=$JAVA_HOME"/bin/java -Djava.security.manager -Djava.security.policy=$PROACTIVE/dist/proactive.java.policy -Dlog4j.configuration=file:${PROACTIVE}/compile/proactive-log4j -Dproactive.home=$PROACTIVE "
    $JAVACMD $@
fi
