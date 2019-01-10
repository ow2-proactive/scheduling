#!/bin/sh

SCRIPT_DIR=$(dirname "$0")
cd "$SCRIPT_DIR"
echo "Running in $SCRIPT_DIR"

echo "Process tree killer test : detached command"

sleep 1000
