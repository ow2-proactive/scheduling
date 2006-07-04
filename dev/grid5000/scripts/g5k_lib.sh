#! /bin/sh

ERR_PREFIX="Grid5000 scripts: "
UNAME=$(uname)

. $(dirname $0)/g5k_lib/sites.sh
. $(dirname $0)/g5k_lib/oar.sh
. $(dirname $0)/g5k_lib/options.sh

ROOT_PASSWD="grid5000"
VERBOSE_SSH=No
SSH_CMD="echo SSH_CMD is not configured"

if [ "$VERBOSE_SSH" == "Yes" ] ; then
	SSH_CMD="ssh  -o NumberOfPasswordPrompts=0"
else
	SSH_CMD='ssh  -o NumberOfPasswordPrompts=0'
fi
