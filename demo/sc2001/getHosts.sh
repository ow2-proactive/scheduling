#!/bin/sh

$LSB_TRAPSIGS

echo
echo --- getHosts ---------------------------------------------

if [ $# -ne 1 ]; then
echo "
    submit a job launching 2 ProActive nodes per host.
    In parameter, the number of ProActive nodes to launch
    that number divided by 2 is the number of hosts
    involved

    ex : getHosts 4

    "
    exit 1
fi

echo "

 Acquiring $1 processors and creating $1 ProActive nodes.

"

bsub -n $1 -q normal -R 'span[ptile=2]' launchNodes.sh

echo
echo ---------------------------------------------------------
