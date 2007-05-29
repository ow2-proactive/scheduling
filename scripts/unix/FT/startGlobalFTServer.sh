#! /bin/bash
if [ -z "$PROACTIVE" ]
then
workingDir=`dirname $0`
PROACTIVE=$workingDir/../../../.
CLASSPATH=.
fi
. $PROACTIVE/scripts/unix/env.sh

$JAVACMD -Djava.protocol.handler.pkgs=org.objectweb.proactive.core.ssh -Xms64m -Xmx1024m org.objectweb.proactive.core.body.ft.servers.StartFTServer "$@"

# DEBUG :   -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n -Djava.compiler=NONE
