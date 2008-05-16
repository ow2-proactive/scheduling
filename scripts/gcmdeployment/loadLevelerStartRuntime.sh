#!/bin/sh

# This script is executed directly on each host
# given back by the loadleveler scheduler.

echo "starting the following command on " `hostname`
echo "-------------------------------------------------------"
echo ${CMD}
echo "-------------------------------------------------------"
eval ${CMD}
