#! /bin/sh

ERR_PREFIX="Grid5000 scripts: "
UNAME=$(uname)

. $(dirname $0)/g5k_lib/sites.sh
. $(dirname $0)/g5k_lib/oar.sh
. $(dirname $0)/g5k_lib/options.sh

ROOT_PASSWD="grid5000"


VERBOSE_SSH=No
SSH_CMD="echo SSH_CMD is not configured"

get_cluster 2>&1 > /dev/null 
onGrid5000=$?
if [ "$onGrid5000" -eq 1 ] ; then
	SSH_CMD="ssh acces.sophia.grid5000.fr ssh -o NumberOfPasswordPrompts=0"
else
	SSH_CMD="ssh -o NumberOfPasswordPrompts=0"
fi


if [ "$VERBOSE" == "Yes" ] ; then
	echo SSH_CMD=$SSH_CMD
fi
