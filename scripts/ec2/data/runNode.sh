#!/bin/bash
#
# runNode.sh
#
# Start a ProActive Runtime on an EC2 Instance,
# register it on a remote Resource Manager
#


# generates a pseudo-random nodename prefixed with EC2
# and containing the instance type
function rand_name {
    a="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    while [ "${n:=1}" -le "16" ]
    do  name="$name${a:$(($RANDOM%${#a})):1}"
	let n+=1
    done
    ITYPE=$(curl http://169.254.169.254/2009-04-04/meta-data/instance-type \
	2>/dev/null);

    echo "EC2-$(echo $ITYPE|sed -r s/[.]/-/g)$name"
}

source env.sh

# user-data contains mandatory info for node startup,
curl http://169.254.169.254/1.0/user-data > ec2node.properties

# passing through the NAT requires knowing the public IP
IP_ADDRESS=$(curl http://169.254.169.254/2009-04-04/meta-data/public-ipv4 \
    2>/dev/null)

# extract the protocol and port from the user-data
RMURL=$(cat ec2node.properties|grep rmUrl \
    |sed -r "s/rmUrl[\t\s ]*[=][\t\s ]*(.*)[\t\s ]*/\1/")

if [ $(echo $RMURL| grep "^http://"| wc -l) = "1" ] ; then
    PROTOCOL="http"
    PORT_PROP="proactive.http.port"
elif [ $(echo $RMURL| grep "^rmi://"| wc -l) = "1" ] ; then
    PROTOCOL="rmi"
    PORT_PROP="proactive.rmi.port"
elif [ $(echo $RMURL| grep "^rmissh://"| wc -l) = "1" ] ; then
    PROTOCOL="rmissh"
    PORT_PROP="proactive.ssh.port"
else
    echo "Protocol not supported for RM Url: $RMURL"
    echo "Aborting."
    exit 1
fi

PORT=$(echo $RMURL| sed -r "s/.*[:]([0-9]+)[/]?/\1/")

# parse property file
USER=$(cat ec2node.properties|grep rmLogin \
    |sed -r "s/rmLogin[\t\s ]*[=][\t\s ]*(.*)[\t\s ]*/\1/")
PASS=$(cat ec2node.properties|grep rmPass \
    |sed -r "s/rmPass[\t\s ]*[=][\t\s ]*(.*)[\t\s ]*/\1/")
NODE=$(rand_name)
NS=$(cat ec2node.properties|grep nodeSource \
    |sed -r "s/nodeSource[\t\s ]*[=][\t\s ]*(.*)[\t\s ]*/\1/")

# starts the node
$JAVA_HOME/bin/java -cp $JARS \
    -Dproactive.home=$PROACTIVE_HOME \
    -Dpa.scheduler.home=$PA_SCHEDULER \
    -Dpa.rm.home=$PA_SCHEDULER \
    -Dproactive.communication.protocol=$PROTOCOL \
    -D$PORT_PROP=$PORT \
    -Dproactive.hostname=$IP_ADDRESS \
    -server org.ow2.proactive.resourcemanager.utils.PAAgentServiceRMStarter \
    $USER $PASS $RMURL $NODE $NS
