#@IgnoreInspection BashAddShebang
function cleanLogs {
    rm -f $SCHEDULER_HOME/logs/*.log
}

function waitServer {
    echo "Wait until server Scheduler has been started..."
    local lines="0"
    while [ $lines -eq "0" ]
    do
        lines=$(grep "Get started" $SCHEDULER_LOG_FILE 2>/dev/null | wc -l )
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
    done

}

function startNode {
    echo "Start node with $N_WORKERS workers"
    $SCHEDULER_HOME/bin/proactive-node -Dproactive.communication.protocol=pnp -Dproactive.node.reconnection.attempts=100 -r pnp://${PNP_SERVER}:64738 -w $N_WORKERS &
}

function killServer {
    echo "Kill scheduler"
    pkill -SIGKILL -f org.ow2.proactive.scheduler.util.SchedulerStarter
}

function killNode {
    echo "Kill node"
    pkill -SIGKILL -f org.ow2.proactive.resourcemanager.utils.RMNodeStarter
}

function removeDb {
    s="rm -f -r $SCHEDULER_HOME/data/db/rm"
    eval $s
    rm -f -r $SCHEDULER_HOME/data/db/scheduler
}

function shutdownEverything {
    killServer
    killNode
    removeDb
}

function saveRecords {
    echo "type-of-test; start-time; n-workers; startup-time; status; actually-recovered" >> $STORAGE_FILE
    recordLine="$1;$2;$3;$4;$5;$6"
    echo $recordLine
    echo $recordLine >> $STORAGE_FILE
    mkdir -p $STORAGE/$2
    cp $SCHEDULER_HOME/logs/* $STORAGE/$2/
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
    SESSIONID="$(curl -s --data "username=$USERNAME&password=$PASSWORD" $SERVER/rest/studio/login)"
}

function submitJob {
    local JOBID=$(curl -s -H "sessionid:$SESSIONID" -F "file=@$LONG_JOB_PATH;type=application/xml" "$SERVER/rest/scheduler/submit" | jq -r '.id')
    echo $JOBID
    return $JOBID
}

function progressBar {
    local TOTAL_LENGTH_OF_PROGRESS=50
    local DONE_BLOCKS=$(echo "$1*$TOTAL_LENGTH_OF_PROGRESS/$2" | bc)
    local DONE_PERCENTAGE=$(echo "scale=2; $1*100/$2" | bc -l)
    local COUNT=0
    s="["
    while [ $COUNT -lt $DONE_BLOCKS ]
    do
        s="$s#"
        let "COUNT=COUNT+1"
    done
    while [ $COUNT -lt $TOTAL_LENGTH_OF_PROGRESS ]
    do
        s="$s "
        let "COUNT=COUNT+1"
    done
    s="$s] ($DONE_PERCENTAGE%)"
    if [ $1 -ne $2 ]
    then
        s="$s\r"
        echo -ne "$s"
    else
        echo "$s"
    fi
}


function countRunningJob {
    RUNNING_JOBS=0
    echo "Counting running jobs..."
    c=0
    for jobid in "${jobids[@]}"
    do
        status=$(curl -s -H "sessionid:$SESSIONID"  "$SERVER/rest/scheduler/jobs/$jobid/" | jq -r '.jobInfo.status')
        if [ $status = "RUNNING" ]
        then
            let "RUNNING_JOBS=RUNNING_JOBS+1"
        fi
        progressBar $c ${#jobids[@]}
        let "c=c+1"
    done
    progressBar $c ${#jobids[@]}
}

function waitRunningJob {
    RUNNING_JOBS=0
    for jobid in "${jobids[@]}"
    do
        local status="notrunning"
        while [ $status != "RUNNING" ]
        do
            status=$(curl -s -H "sessionid:$SESSIONID"  "$SERVER/rest/scheduler/jobs/$jobid/" | jq -r '.jobInfo.status')
        done
        progressBar $RUNNING_JOBS ${#jobids[@]}
        let "RUNNING_JOBS=RUNNING_JOBS+1"
    done
    progressBar $RUNNING_JOBS ${#jobids[@]}
}

function waitKilledJob {
    local status="non-empty-srting"
    while [ $status != "KILLED" ] && [ $status != "FINISHED" ] && [ $status != "STALLED" ] && [ $status != "CANCELED" ] ;
    do
        status=$(curl -s -H "sessionid:$SESSIONID"  "$SERVER/rest/scheduler/jobs/$jobid/" | jq -r '.jobInfo.status')
    done
}



function killAndDeleteAllJobs {
    local i=0
    echo "Killing and deleting jobs..."
    for jobid in "${jobids[@]}"
    do
        curl -X PUT -s -H "sessionid:$SESSIONID"  "$SERVER/rest/scheduler/jobs/$jobid/kill/" > /dev/null
        waitKilledJob $jobid
        curl -X DELETE -s -H "sessionid:$SESSIONID"  "$SERVER/rest/scheduler/jobs/$jobid/" > /dev/null
        progressBar $i ${#jobids[@]}
        let "i=i+1"
    done
    progressBar $i ${#jobids[@]}
}


function getStartupTime {
    START_TIME=$(getMilliseconds "Starting the scheduler")
    END_TIME=$(getMilliseconds "Get started")
    STARTUP_TIME=`expr $END_TIME - $START_TIME`
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

    getStartupTime


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
    declare -a jobids

    removeDb
    cleanLogs

    startServer
    waitServer

    startNode
    waitNode

    loginAndReturnSessionId

    COUNT=0
    echo "Submitting jobs..."
    while [ $COUNT -lt $N_WORKERS ]
    do
        NEWJOBID=$(submitJob)
        jobids+=($NEWJOBID)
        progressBar $COUNT ${N_WORKERS}
        let "COUNT=COUNT+1"
    done
    progressBar $COUNT ${N_WORKERS}

    echo "Counting running jobs..."

    waitRunningJob

    echo "Running jobs $RUNNING_JOBS"

    if [ $RUNNING_JOBS -eq $N_WORKERS ]
    then
        killServer

        cleanLogs

        startServer
        waitServer
        loginAndReturnSessionId

        getStartupTime

        countRunningJob

        STATUS="FAILURE"
        if [ $RUNNING_JOBS -eq $N_WORKERS ]
        then
            STATUS="SUCCESS"
        fi

        saveRecords "task-recovery" $START_TIME $N_WORKERS $STARTUP_TIME $STATUS $RUNNING_JOBS

    else

        echo "test failed, workers = " $N_WORKERS "but running jobs is " $RUNNING_JOBS

    fi

    echo "before killing and deleting jobs"
    killAndDeleteAllJobs
    shutdownEverything

}
