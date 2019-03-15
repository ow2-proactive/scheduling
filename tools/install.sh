#!/bin/bash

SCRIPT_DIR="$(dirname "$0")"

DT=$(date +"%m-%d-%y")

"$SCRIPT_DIR/install_base.sh" 2>&1 | tee -a install-${DT}.log
