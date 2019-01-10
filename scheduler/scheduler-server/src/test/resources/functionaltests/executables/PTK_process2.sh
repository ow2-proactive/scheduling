#!/bin/sh

SCRIPT_DIR=$(dirname "$0")
cd "$SCRIPT_DIR"
echo "Running in $SCRIPT_DIR"

echo "Process tree killer test : detached command 2"

# wait for 5 seconds but not with sleep!
ping -c 5 127.0.0.1
