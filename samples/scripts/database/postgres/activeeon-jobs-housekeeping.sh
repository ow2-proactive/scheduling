#!/bin/bash

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LOG_FILE=$CURRENT_DIR/activeeon-jobs-housekeeping.log
PLPGSQL_FILE=$CURRENT_DIR/activeeon-jobs-housekeeping-invoke.plpgsql

function log() {
    echo -e $1 2>&1 >> $LOG_FILE
}


log "Starting housekeeping $(date)\n"

/usr/bin/psql scheduler postgres < $PLPGSQL_FILE 2>&1 >> $LOG_FILE

log "End $(date)\n\n\n"
