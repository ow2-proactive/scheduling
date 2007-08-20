#!/bin/sh

# 
# The following variable should be automatically
# assigned during install, if not, edit it to reflect
# your Java installation.
#
workingDir=`dirname $0`



#
# You don't need to edit the following line
#

# exec ${JAVA_HOME}/bin/java -Djava.security.manager   -Djava.security.policy=/home/vlegrand/.java.policy -Doscar.system.properties=/user/vlegrand/home/Oscar/lib/system.properties  -Dcom.sun.management.jmxremote -jar lib/oscar.jar -Dlog4j.configuration=file:///user/vlegrand/home/SVN/proactive/trunk/scripts/proactive-log4j

##exec ${JAVA_HOME}/bin/java -Djava.security.manager  -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8100 -Djava.security.policy=/home/vlegrand/.java.policy  -Dcom.sun.management.jmxremote   -Doscar.system.properties=/user/vlegrand/home/Oscar/lib/system.properties  -Dlog4j.configuration=file:///user/vlegrand/home/SVN/proactive/trunk/scripts/proactive-log4j -jar lib/oscar.jar
echo $1
OSCAR_HOME="."

echo ${HOSTNAME}
MYHOSTNAME=`hostname --long`
echo $MYHOSTNAME

rm -rf cache/${MYHOSTNAME}/*

${JAVA_HOME}/bin/java -Doscar.cache.profiledir="${OSCAR_HOME}/cache/${MYHOSTNAME}"  -Doscar.cache.profile="${MYHOSTNAME}"  -Djava.security.policy="${OSCAR_HOME}/etc/java.policy"  -Dcom.sun.management.jmxremote -Dproactive.http.servlet=enabled -Dproactive.communication.protocol=http -Dproactive.http.port=8080  -Doscar.system.properties="${OSCAR_HOME}/etc/system.properties"   -jar "$OSCAR_HOME/../../../lib/osgi/oscar.jar"
#-Djava.security.manager