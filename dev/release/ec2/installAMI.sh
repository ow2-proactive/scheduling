#!/bin/bash
#
# installAMI.sh
#
########################################################################### 80 #
#
# Installs ProActive components on an EC2 running Instance,
# meant to be run by createAMI.sh: it requires a specific environment
# built by createAMI.sh. Those scripts should be located in the same
# directory.
#
# Valid options:
#
# -pa URL           URL to an archive containing the PA Scheduling sources
# -java URL         URL to an archive containing  a JDK (>=5.0), 
# -w                Wait before bundling, useful for connecting on the instance
#                   to run custom commands before creating the AMI
# -x64              Create a x86_64 AMI instead of i386, will work
#                   only if the instance is a c1.xlarge or m1.xlarge
# -nr               No Runtime: do not launch a ProActive runtime at boot;
#                   use this if you want to install ProActive Programming
#                   and not ProActive Scheduling on your instance
#
#
# Note: the URLs can be HTTP, HTTPS or FTP; the archives have to be .tar.bz2,
# .tar.gz, .tar or .zip (extensions need to match).
#


#
# error handling
#
function error {
    echo "Fatal error, aborting."
    exit 1
}
trap error ERR

WAIT_END=false
ARCH=i386
NO_RUNTIME=false

#
# Arguments handling
#
while [ $# -gt 0 ]; do
    case $1 in
	-pa)               PA_ARCHIVE=$2;                 shift 2 ;;
	-java)             JAVA_ARCHIVE=$2;               shift 2 ;;
	-w)                WAIT_END=true;                 shift 1 ;;
	-x64)              ARCH=x86_64;                   shift 1 ;;
	-nr)               NO_RUNTIME=true;               shift 1 ;;
	*)                 echo "$0: invalid option $1" >&2
    esac
done

#
# Extracts the archive $1 in $2.
# Supported formats are tar.bz2, tar.gz, tar and zip;
# extensions need to match exactly for the detection to work
#
function open_archive {
    if [ $(echo $1 |sed -r "s/.*([.]tar[.]bz2)$/\1/") = ".tar.bz2" ]; then
	tar jxf $1 -C $2
    elif [ $(echo $1 |sed -r "s/.*([.]zip)$/\1/") = ".zip" ]; then
	unzip $1 -d $2
    elif [ $(echo $1 |sed -r "s/.*([.]tar[.]gz)$/\1/") = ".tar.gz" ]; then
	tar zxf $1 -C $2
    elif [ $(echo $1 |sed -r "s/.*([.]tar)$/\1/") = ".tar" ]; then
	tar xf $1 -C $2
    else
	echo "Unable to handle archive: $1"
	echo -n "Supported types (extensions need to match): "
	echo " .zip, .tar, .tar.gz, .tar.bz2"
	exit 1
    fi
}


#
# If the archive expanded in $1 had its content
# in a subdirectory, it is moved to the parent directory.
# This allows /some/ flexibility regarding the specified archive,
# which can be the one created by createAMI.sh, or a release
# from a remote repository
#
function fit_archive {
    if [ $(ls -1 $1| wc -l) = "1" ]; then
	DIR=$1/$(ls -1 $1)
	mv $DIR/* $1
	rm -Rf $DIR
    fi
}

#
# unpack archives
#
echo "Unpacking archives..."
mkdir /usr/share/ProActive /usr/share/EC2Node /root/JDK
cd /tmp


# if this succeeds, an HTTP archive was specified for PA,
# trying to download and extract it
if [ "$PA_ARCHIVE" != "" ] ; then
    wget $PA_ARCHIVE &>/dev/null
    open_archive "/tmp/$(basename $PA_ARCHIVE)" /usr/share/ProActive
else
    tar jxf /tmp/PA.tar.bz2 -C /usr/share/ProActive
fi
fit_archive /usr/share/ProActive

# if this succeeds, an HTTP archive was specified for Java,
# trying to download and extract it
if [ "$JAVA_ARCHIVE" != "" ] ; then
    wget $JAVA_ARCHIVE &>/dev/null
    open_archive "/tmp/$(basename $JAVA_ARCHIVE)" /root/JDK
else
    tar jxf /tmp/JDK.tar.bz2 -C /root/JDK
fi
fit_archive /root/JDK

# unpack EC2 archive
mkdir /usr/share/ec2-tools/
tar jxf /tmp/EC2.tar.bz2 -C /usr/share/ec2-tools/
chmod -R 755 /usr/share/ec2-tools/


echo "Setting environment..."
chmod 755 /root/ec2-cred.sh
source /root/ec2-cred.sh
export JAVA_HOME=/root/JDK
echo "export JAVA_HOME=/root/JDK" >> /root/.bashrc
echo "export PROACTIVE_HOME=/usr/share/ProActive/" >> /root/.bashrc


cat <<EOF > /root/.java.policy
grant {
        permission java.security.AllPermission;
};
EOF
chmod 755 /root/.java.policy

#
# installing startup scripts
#

if $NO_RUNTIME ; then
    echo "Skipping startup scripts installation"
else
    echo "Installing scripts..."
    cp /tmp/runNode.sh /tmp/params.py \
	/usr/share/EC2Node/
    chmod 755 /usr/share/EC2Node/*
    
    cat <<EOF > /usr/local/bin/startNode.sh
#!/bin/bash

cd /usr/share/EC2Node
./runNode.sh

EOF
    chmod 755 /usr/local/bin/startNode.sh
    # will make the scripts run at boot
    # change /etc/rc.local to whatever script is run last at boot
    # if the AMI used is not using this at startup.
    # This should work fine for at least all Fedora
    # and Debian GNU/Linux based systems
    #
    
    # prefix all 'exit 0' with a startnode
    sed 's/exit 0/\/usr\/local\/bin\/startNode.sh | logger -s -t \"ProActive\"; exit 0/g' \
	</etc/rc.local >/etc/rc.local.new
    cat /etc/rc.local.new > /etc/rc.local
    
    # add a startnode at the end of the script, in case there was no exit clause
    echo "/usr/local/bin/startNode.sh | logger -s -t \"ProActive\"" >> /etc/rc.local
    echo "exit 0" >> /etc/rc.local
    
    chmod a+x /etc/rc.local
fi

echo "All components were successfully installed."

# cleanup
rm -Rf /tmp/*bz2 &>/dev/null


# some customization
cat <<EOF > /etc/motd
  _ \              \        |  _)             .
 |   |  __| _ \   _ \   __| __| |\ \   / _ \  .
 ___/  |   (   | ___ \ (    |   | \ \ /  __/  .
_|    _|  \___/_/    _\___|\__|_|  \_/ \___|  .

   Welcome to a ProActive EC2 Image ;-)

       http://proactive.inria.fr/

EOF

echo "PS1='[\u@ProActive-ec2:\W]\$ '" >> /root/.bashrc

# wait if necessary
if $WAIT_END ; then
    echo "Waiting... If you need to run custom commands before bundling, now is the time."
    ANS="no"
    while [ "$ANS" != "continue" ] ; do
	echo "Type 'continue' to continue and bundle the instance. "
	read ANS
    done
fi

#
# bundling, uploading, registering new AMI
#

# user-data contains the AMI name
AMI_NAME=$(curl http://169.254.169.254/1.0/user-data 2>/dev/null)

echo "Bundling volume... ($ARCH)"
ec2-bundle-vol -c $EC2_CERT -k $EC2_PRIVATE_KEY -u $EC2_USER \
    -p $AMI_NAME -d /tmp/ -r $ARCH &>/dev/null

echo "Uploading bundle..."
ec2-upload-bundle -b $BUCKET \
    -m /tmp/$AMI_NAME.manifest.xml -a $AWS_AKEY -s $AWS_SKEY \
    -d /tmp/ &>/dev/null

echo "Registering new AMI: $AMI_NAME"
ec2-register $BUCKET/$AMI_NAME.manifest.xml &>/dev/null

echo "All done, exiting"

exit 0
