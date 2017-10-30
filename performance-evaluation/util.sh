function cleanLogs {
    rm -f $SCHEDULER_HOME/logs/*.log
}

function waitServer {
    echo "Wait until server Scheduler has been started"
    lines="0"
    while [ $lines -eq "0" ]
    do
        lines=$(grep "Get started" $SCHEDULER_LOG_FILE 2>/dev/null | wc -l )
#		echo $lines
            sleep 1
    done

}

function startServer {
    echo "Start scheduler with 0 nodes"
    $SCHEDULER_HOME/bin/proactive-server -ln 0 &
}



function waitNode {
    echo "Wait until node has been started"
    lns="0"
    while [ $lns -eq "0" ]
    do
        lns=$(grep "Connected to the resource manager at " $NODE_LOG_FILE 2>/dev/null | wc -l )
#		echo $lns
            sleep 1
    done

}

function startNode {
    echo Start node with N workers
    $SCHEDULER_HOME/bin/proactive-node -w $N_WORKERS -Dproactive.node.reconnection.attempts=100 &
}

function killServer {
    echo "Kill scheduler"
    pkill -SIGKILL -f org.ow2.proactive.scheduler.util.SchedulerStarter
}

function killNode {
    echo "Kill node"
    pkill -SIGKILL -f org.ow2.proactive.resourcemanager.utils.RMNodeStarter
}


function shutdownEverything {
    killServer
    killNode
}

function saveRecords {
    recordLine="$1;$2;$3;$4;$5;$6"
    echo $recordLine
    echo $recordLine >> $STORAGE_FILE
    mkdir -p $STORAGE/$2
    cp $SCHEDULER_HOME/logs/* $STORAGE/$1/
}

function getMilliseconds {
    S=$(eval "grep \"$1\" $SCHEDULER_LOG_FILE | cut -d' ' -f1-2")
    DATE=${S:1}
    RET=$(date -d "$DATE" +'%s%3N')
    echo $RET
    return $RET
}

function countRecoveredNodes {
    RET=$(grep "Node to recover could successfully be looked" $SCHEDULER_LOG_FILE | wc -l)
    echo $RET
    return $RET
}

function loginAndReturnSessionId {
    RET=$(curl --data "username=$USERNAME&password=$PASSWORD" $SERVER/rest/studio/login)
    echo $RET
    return $RET
}

function submitJob {
    curl -H "sessionid:$SESSIONID" -F "file=@$LONG_JOB_PATH;type=application/xml" "$SERVER/rest/scheduler/submit"
}

function testNodeRecovery {
    cleanLogs

    startServer
    waitServer

    startNode
    waitNode

    killServer

    cleanLogs

    startServer
    waitServer

    START_TIME=$(getMilliseconds "Starting the scheduler")

    END_TIME=$(getMilliseconds "Get started")

    STARTUP_TIME=`expr $END_TIME - $START_TIME`

    RECOVERED_NODES=$(countRecoveredNodes)
    STATUS="FAILURE"
    echo "RECOVERED_NODES" $RECOVERED_NODES
    if [ $RECOVERED_NODES -eq $N_WORKERS ]
    then
        STATUS="SUCCESS"
    fi
    echo "STATUS" $STATUS

    saveRecords "node-recovery" $START_TIME $N_WORKERS $STARTUP_TIME $STATUS $RECOVERED_NODES

    shutdownEverything

}


function testTaskRecovery {
    cleanLogs

    startServer
    waitServer

    startNode
    waitNode

    SESSIONID=$(loginAndReturnSessionId)
    COUNT=0
    while [ $COUNT -lt $N_WORKERS]
    do
        submitJob
        let "COUNT=COUNT+1"
    done

    numberRunningJobs

    killServer

    cleanLogs

    startServer
    waitServer

    numberRunningJobs

    shutdownEverything
}
