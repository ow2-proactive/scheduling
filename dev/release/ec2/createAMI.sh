#!/bin/bash
#
# createAMI.sh
#
########################################################################### 80 #
#
# EC2 AMI creation utility
#
# Creates AMI suited for ProActive Scheduling:
# instances using AMIs created with this script
# can be used in the ProActive Resource Manager
# using the EC2 Infrasture Manager.
# This script can create only Linux images,
# refer to the documentation for Windows image creation
#
#
# To create an AMI, this script will:
# 1. Boot an existing AMI; default is the official Fedora,
#   but it can also be another GNU/Linux distribution.
#   using a system too different from a Fedora may require to adapt
#   the script.
# 2. Create a new instance using the initial image.
# 3. Send archives on the instance for ProActive Scheduling,
#   JDK, and the EC2 tools required to bundle the image.
#   The default procedure is to create archives using default locations,
#   but other archives can be specified. For PA and the JDK,
#   archives on a remote HTTP/FTP server can also be specified.
# 4. Execute on the instance an installation script
#   that will download, unpack and install the archives for PA, JDK and EC2.
# 5. Install scripts so that a PA node registers to a remote Resource
#   Manager when the image is booted.
#
#
# try -h for help
#
#
#
#

cd $(dirname $0) &>/dev/null

# error handling
CAN_FAIL=false
function error {
    if $CAN_FAIL ; then
	CAN_FAIL=false
	return 0
    fi
    echo "Fatal error, aborting."
    exit 1
}
trap error ERR

# echoes $1 as an absolute path
function get_absolute {
    cd $1
    pwd
    cd - &>/dev/null
}

function print_help {
echo -ne " \033[1m$0\033[0m \033[1m[\033[0m\033[4moptions"
echo -e "\033[0m\033[1m]\033[0m\n"
echo -e " Creates a new AMI, installs all required components"
echo -e " to run it as EC2 node in the Resource Manager"
echo -e " and registers it in you bucket.\n"
echo -e " Available options:\n"
echo -e " \033[1m-a\033[0m, \033[1m--ami\033[0m \033[4mAMI\033[0m"
echo -e "                    Initial AMI identifier, defaults to ami-5647a33f, "
echo -e "                    the latest officially supported Fedora8 image."
echo -e " \033[1m-w\033[0m, \033[1m--wait\033[0m"
echo -e "                    When the install script is done, just before bundling"
echo -e "                    and uploading the AMI, the script will wait for user input"
echo -e "                    before continuing. This allows the user to run commands"
echo -e "                    on the instance with SSH to customize it."
echo -e "                    When running the script in non-interactive mode,"
echo -e "                    this options will be ignored."
echo -e " \033[1m-r\033[0m, \033[1m--no-runtime\033[0m"
echo -e "                    Do not make a ProActive runtime start at boot"
echo -e "                    on the instance; use this if you do not want"
echo -e "                    to automatically register to a remote Resource Manager,"
echo -e "                    or if you want to bundle a ProActive Programming archive."
echo -e " \033[1m-x\033[0m, \033[1m--x86_64\033[0m"
echo -e "                    Use this switch when bundling an x86_64 AMI"
echo -e "                    instead of the default i386. The initial AMI"
echo -e "                    has to be x86_64 arch, and to bundle such AMI,"
echo -e "                    the script will need to start an Extra Large"
echo -e "                    instance, which is much more expensive than the"
echo -e "                    default, small."
echo -e " \033[1m-p\033[0m, \033[1m--sched-path\033[0m \033[4mPATH\033[0m"
echo -e "                    Path to the ProActive Scheduler sources,"
echo -e "                    defaults to \$PWD/../../.., assuming this script"
echo -e "                    is in its default location."
echo -e "                    \033[4mPATH\033[0m can also be a zip, tar.bz2,"
echo -e "                    tar.gz or tar archive, located on the local"
echo -e "                    filesystem or on a remote HTTP server."
echo -e " \033[1m-j\033[0m, \033[1m--java-path\033[0m \033[4mPATH\033[0m"
echo -e "                    Path to the Java installation path, defaults to"
echo -e "                    \$JAVA_HOME."
echo -e "                    \033[4mPATH\033[0m can also be a zip, tar.bz2,"
echo -e "                    tar.gz or tar archive, located on the local"
echo -e "                    filesystem or on a remote HTTP server."
echo -e " \033[1m-e\033[0m, \033[1m--ec2-path\033[0m \033[4mPATH\033[0m"
echo -e "                    Path to the EC2 CLI tools installation path, defaults to"
echo -e "                    \$EC2_HOME. As opposed to Java and ProActive, this"
echo -e "                    cannot be a previously packed or remote archive."
echo -e " \033[1m-n\033[0m, \033[1m--name\033[0m \033[4mNAME\033[0m"
echo -e "                    Name of the new AMI, will be asked interactively"
echo -e "                    if not specified here."
echo -e "                    Try to avoid naming conflicts by listing your"
echo -e "                    AMIs prior to calling this script."
echo -e " \033[1m-i\033[0m, \033[1m--non-interactive\033[0m"
echo -e "                    Non interactive mode: no user input is required,"
echo -e "                    useful for automation."
echo -e " \033[1m-o\033[0m, \033[1m--overwrite\033[0m"
echo -e "                    Overwrite existing files without interactive"
echo -e "                    confirmation."
echo -e " \033[1m-c\033[0m, \033[1m--no-http-check\033[0m"
echo -e "                    Do not perform HTTP check when providing archives"
echo -e "                    as HTTP URLs. When using this switch, URLs need"
echo -e "                    to be prefixed with 'http://'."
echo -e " \033[1m-h\033[0m, \033[1m--help\033[0m"
echo -e "                    Print this message and exit."
}


# if false, no user input will be required, useful for automated calls
INTERACTIVE=true
# if true, existing files will be overwritten without warning
OVERWRITE=false
# if false, do not perform HTTP check when providing archives as HTTP URLs
HTTP_CHECK=true
# if true, open an SSH prompt after installation
WAIT_END=false
# one of i386 or x86_64
ARCH=i386
# m1.small for i386, m1.xlarge for x86_64
INSTANCE_TYPE=m1.small

# if true, PA / JDK archives will be downloaded directly on the network
# from the instance, instead of archiving and sending it
PA_HTTP=false
JDK_HTTP=false

# do not autostart PA runtime if true
NO_RUNTIME=false

#
# Builds archive from directory, or echoes path if already a valid archive
#
# Parameters :
# $1 : Directory or archive. Supported formats:
#      .tar, .zip, .tar.gz, .tar.bz2; if names don't match the extension,
#      the type will not be detected
# $2 : Name of the archive for debug
# $3 : Default destination file if $1 is a directory
# $4 : Optional exclude list for tar when specifying a directory
#
# Result :
GET_ARCHIVE_RET=/dev/null
# will put the actual archive location in global variable GET_ARCHIVE_RET
#
# Returns :
# 0 : Nothing special
# 1 : $1 was detected as an URL
#
function get_archive {
    if [ $# -lt 2 ]; then
	echo "get_archive: not enough parameters"
	exit 1
    fi

    if $HTTP_CHECK ; then
	HTTP_RESPONSE=$(curl -m 5 -I -L $1 2>/dev/null|grep "HTTP/1"|tail -n 1)
	if [ "$HTTP_RESPONSE" != "" ]; then
	    echo -n "$2: found URL $1 ($HTTP_RESPONSE), use? [y/n] "
	    read http_answ
	    if [ "$http_answ" != "y" ]; then
		echo "Aborting."
		exit 0
	    else
		GET_ARCHIVE_RET=$1
		echo "Using HTTP archive for $2: $1"
		CAN_FAIL=true
		return 1
	    fi
	fi
    else
	if [ $(echo $1|grep "http://"|wc -l) = "1" ]; then
	    GET_ARCHIVE_RET=$1
	    echo "Using HTTP archive for $2: $1"
	    CAN_FAIL=true
	    return 1
	fi
    fi

    if [ ! -e $1 ]; then
	echo "$1: no such file or directory"
	exit 1
    fi

    if [ -d $1 ]; then
	if [ -e $3 ]; then
	    if $OVERWRITE ; then
		echo "$3 exists: Overwriting."
	    elif $INTERACTIVE ; then
		echo -n "$3 exists: overwrite [y/n], use [u] ? "
		read answer
		if [ "$answer" = "u" ]; then
		    echo "Using existing archive $3 for $2"
		    GET_ARCHIVE_RET=$3
		    return 0;
		elif [ "$answer" != "y" ]; then
		    echo "Aborting."
		    exit 0
		fi
	    else
		echo "$3: file exists."
		exit 1
	    fi
	fi

	echo "Creating $2 archive from $1 in $3"
	cd $1
	tar jcf $3 . $(if [ $# -gt 3 ]; then echo "-X $4"; fi) >/dev/null
	cd - &> /dev/null
	GET_ARCHIVE_RET=$3

    elif [ $(echo $1 |sed -r "s/.*([.]tar[.]bz2)$/\1/") = ".tar.bz2" ]; then
	echo "Found existing bz2 archive for $2: $1"
	GET_ARCHIVE_RET=$1
    elif [ $(echo $1 |sed -r "s/.*([.]zip)$/\1/") = ".zip" ]; then
	echo "Found existing zip archive for $2: $1"
	GET_ARCHIVE_RET=$1
    elif [ $(echo $1 |sed -r "s/.*([.]tar[.]gz)$/\1/") = ".tar.gz" ]; then
	echo "Found existing gz archive for $2: $1"
	GET_ARCHIVE_RET=$1
    elif [ $(echo $1 |sed -r "s/.*([.]tar)$/\1/") = ".tar" ]; then
	echo "Found existing tar archive for $2: $1"
	GET_ARCHIVE_RET=$1
    else
	echo "Unable to handle archive: $1"
	exit 1
    fi

    return 0
}


#
# parse options
#
while [ $# -gt 0 ]; do
    case $1 in
	-a)                AMI=$2;                         shift 2 ;;
	--ami)             AMI=$2;                         shift 2 ;;
	-w)                WAIT_END=true;                  shift 1 ;;
	--wait)            WAIT_END=true;                  shift 1 ;;
	-x)                ARCH=x86_64;                    shift 1 ;;
	-x86_64)           ARCH=x86_64;                    shift 1 ;;
	-p)                SCHEDULER_PATH=$2;              shift 2 ;;
	--sched-path)      SCHEDULER_PATH=$2;              shift 2 ;;
	-j)                JAVA_PATH=$2;                   shift 2 ;;
	--java-path)       JAVA_PATH=$2;                   shift 2 ;;
	-e)                EC2_HOME=$2;                    shift 2 ;;
	--ec2-path)        EC2_HOME=$2;                    shift 2 ;;
	-n)                NAME=$2;                        shift 2 ;;
	--name)            NAME=$2;                        shift 2 ;;
	-i)                INTERACTIVE=false;              shift 1 ;;
	--non-interactive) INTERACTIVE=false;              shift 1 ;;
	-o)                OVERWRITE=true;                 shift 1 ;;
	--overwrite)       OVERWRITE=true;                 shift 1 ;;
	-c)                HTTP_CHECK=false;               shift 1 ;;
	--no-http-check)   HTTP_CHECK=false;               shift 1 ;;
	-r)                NO_RUNTIME=true;                shift 1 ;;
	--no-runtime)      NO_RUNTIME=true;                shift 1 ;;
	-h)                print_help;                     exit 0;;
	--help)            print_help;                     exit 0;;
	*)                 echo "$0: invalid option $1" >&2;
	                   echo "Try -h for help" ; exit 1;
    esac
done

#
# default values
#
if [ "$AMI" = "" ]; then
    AMI=ami-5647a33f
fi

if [ "$SCHEDULER_PATH" = "" ]; then
    SCHEDULER_PATH=$(get_absolute ../../..)
fi

if [ "$JAVA_PATH" = "" ]; then
    JAVA_PATH=$JAVA_HOME
fi

# checks the mandatory variables are set for EC2 tools
if [ "$EC2_HOME" = "" ] ; then
    echo "EC2_HOME is not set, aborting."; exit 1;
elif [ ! -d $EC2_HOME ] ; then
    echo "EC2_HOME is set but $EC2_HOME is not a directory... Aborting."
    exit 1
elif [ ! -e $EC2_HOME/bin/ec2-run-instances ] ; then
    echo "Could not stat $EC2_HOME/bin/ec2-run-instances... "
    echo "Is EC2_HOME set to a valid EC2 Tools installation directory ? Aborting."
    exit 1
fi
if [ "$EC2_PRIVATE_KEY" = "" ] ; then
    echo "EC2_PRIVATE_KEY is not set, aborting."; exit 1;
fi
if [ "$EC2_CERT" = "" ] ; then
    echo "EC2_CERT is not set, aborting."; exit 1;
fi
if [ "$AWS_AKEY" = "" ] ; then
    echo "EC2_AKEY is not set, aborting."; exit 1;
fi
if [ "$AWS_SKEY" = "" ] ; then
    echo "EC2_SKEY is not set, aborting."; exit 1;
fi
if [ "$EC2_SSH" = "" ] ; then
    echo "EC2_SSH is not set, aborting."; exit 1;
fi
if [ "$BUCKET" = "" ] ; then
    echo "BUCKET is not set, aborting."; exit 1;
fi


if [ "$NAME" = "" ]; then
    if $INTERACTIVE ; then
	echo -n "Enter a name for your new AMI (leave blank to generate): "
	read NAME
	if [ "$NAME" = "" ]; then
	    NAME="ProActive_"$(date +%F_%Hh%M)
	fi
    else
	NAME="ProActive_"$(date +%F_%Hh%M)
    fi
fi


echo "Using AMI: $AMI, will be named $NAME"
echo "Using Scheduler: $SCHEDULER_PATH"
echo "Using Java: $JAVA_PATH"
echo "Using EC2 tools: $EC2_HOME"

if $INTERACTIVE ; then
    echo -n "Perform AMI creation ? [y/n] "
    read answer
    if [ "$answer" != "y" ]; then
	echo "Aborting."
	exit 0
    fi
else
    echo "Performing AMI creation."
fi

echo " "


#
# prepare all required components
#

# prepare ProActive Scheduling 
if [ $(ls -l $SCHEDULER_PATH/dist/lib/ProActive_ResourceManager.jar \
    2>/dev/null |wc -l) = "0" ] ; then
    if $INTERACTIVE ; then
	echo -n "Some jars seem to be missing... Recompile Scheduling? [y/n] "
	read recomp
	if [ "$recomp" = "y" ]; then
	    echo "Building Scheduling..."
	    cd $SCHEDULER_PATH/compile
	    ./build deploy.all &>/dev/null
	    cd - &>/dev/null
	fi
    else
	echo "Building Scheduling..."
	cd $SCHEDULER_PATH/compile
	./build deploy.all &>/dev/null
	cd - &>/dev/null
    fi
fi


cat <<EOF > /tmp/Excludes
.svn
./bin
./classes
./.classpath
./compile
./dev
./doc
./extensions
./.git
./lib
./.logs
./samples
./scheduler-rcp
./rm-rcp
./SCHEDULER-DB
./LICENCE.txt
./.logs
./.project
./README.txt
./RM-README.txt
./scripts
./.settings
./src
EOF

get_archive $SCHEDULER_PATH "Scheduler" /tmp/PA.tar.bz2 /tmp/Excludes
if [ "$?" = "1" ]; then
    PA_HTTP=true
fi
SCHEDULER_ARCHIVE=$GET_ARCHIVE_RET

# prepare JDK

cat <<EOF > /tmp/Excludes
./sample
./src.zip
./man
./demo
EOF

get_archive $JAVA_PATH "JDK" /tmp/JDK.tar.bz2 /tmp/Excludes
if [ "$?" = "1" ]; then
    JDK_HTTP=true
fi
JAVA_ARCHIVE=$GET_ARCHIVE_RET


# prepare EC2 tools

get_archive $EC2_HOME "EC2 tools" /tmp/EC2.tar.bz2
if [ "$?" = "1" ]; then
    echo "EC2 tools path must be a directory: $EC2_HOME. Aborting"
    exit 1
fi
EC2_ARCHIVE=$GET_ARCHIVE_RET


echo " "

#
# boot remote initial EC2 instance
#

# is this 64 bit ?
if [ "$ARCH" = "x86_64" ] ; then
    if $INTERACTIVE ; then
	echo "Bundling x86_64 AMI requires launching an extra large instance,"
	echo -n "as well as a x86_64 initial AMI. Continue? [y/n] "
	read ARCHANSW
	if [ "$ARCHANSW" != "y" ] ; then
	    echo "Aborting."
	    exit 0
	fi
    else
	echo "Bundling x86_64 AMI: instance will be extra large."
    fi
    INSTANCE_TYPE=m1.xlarge
fi


echo "Launching instance..."

echo $INSTANCE_TYPE

INSTANCE_ID=$($EC2_HOME/bin/ec2-run-instances $AMI -k $EC2_KEYPAIR -d $NAME \
    -t $INSTANCE_TYPE |tail -n 1 \
    |sed -r "s/INSTANCE[\t\s ]+(i-[a-z0-9]{8}).*/\1/")
echo "Got instance: $INSTANCE_ID"


#
# wait for the SSH server to be up
#

echo "Waiting for the instance to boot, please wait..."

while [ 1 ]; do
    INST=$($EC2_HOME/bin/ec2-describe-instances|grep $INSTANCE_ID)
    LINES=$(echo $INST|grep running|wc -l)
    if [ "$LINES" = "1" ]; then
	break
    fi
    sleep 1s
done

INSTANCE_URL=$($EC2_HOME/bin/ec2-describe-instances | \
    grep $INSTANCE_ID|sed -r "s/.*[\t\s ]+(.+amazonaws[.]com).*/\1/")

# sometimes SSH is not up even though the machine is showing as 'running'
# sleeping a while seems to solve the problem
sleep 30s

echo "EC2 instance ready at: $INSTANCE_URL"

#
# send all components on the instance
#

# AWS credentials
cat <<EOF >/tmp/ec2-cred.sh
#!/bin/bash
export EC2_HOME=/usr/share/ec2-tools
export AWS_CALLING_FORMAT="SUBDOMAIN"
EOF
echo "export PATH=\$PATH:\$EC2_HOME/bin" >> /tmp/ec2-cred.sh
echo "export EC2_PRIVATE_KEY=/tmp/$(basename $EC2_PRIVATE_KEY)"\
 >> /tmp/ec2-cred.sh
echo "export EC2_CERT=/tmp/$(basename $EC2_CERT)" >> /tmp/ec2-cred.sh
echo "export EC2_KEYPAIR=$EC2_KEYPAIR" >> /tmp/ec2-cred.sh
echo "export EC2_USER=$EC2_USER" >> /tmp/ec2-cred.sh
echo "export AWS_AKEY=$AWS_AKEY" >> /tmp/ec2-cred.sh
echo "export AWS_SKEY=$AWS_SKEY" >> /tmp/ec2-cred.sh
echo "export BUCKET=$BUCKET" >> /tmp/ec2-cred.sh


echo "Sending all components on instance..."

if $PA_HTTP ; then
    RARGS="-pa $SCHEDULER_ARCHIVE"
else
    FILES="$SCHEDULER_ARCHIVE"
fi
if $JDK_HTTP ; then
    RARGS="$RARGS -java $JAVA_ARCHIVE"
else
    FILES="$FILES $JAVA_ARCHIVE"
fi

if [ "$ARCH" = "x86_64" ] ; then
    RARGS="$RARGS -x64"
fi

if $NO_RUNTIME ; then
    RARGS="$RARGS -nr"
fi

scp -i $EC2_SSH -o StrictHostKeyChecking=no \
    $EC2_ARCHIVE $FILES installAMI.sh \
    $SCHEDULER_PATH/scripts/ec2/runNode.sh \
    $SCHEDULER_PATH/scripts/ec2/params.py \
    /tmp/ec2-cred.sh $EC2_PRIVATE_KEY $EC2_CERT \
    root@$INSTANCE_URL:/tmp/

#
# execute the install script and pray
#

if $WAIT_END ; then
    if $INTERACTIVE ; then
	RARGS="$RARGS -w"
    else
	echo "Warning: using --wait switch in non-interactive mode, ignoring."
    fi
fi



echo "Running install script on remote instance..."
ssh -i $EC2_SSH -o StrictHostKeyChecking=no \
    root@$INSTANCE_URL /tmp/installAMI.sh $RARGS \
    | sed 's/.*/\t[EC2] &/'

#
# terminate the instance
#

echo "Shutting down instance..."
$EC2_HOME/bin/ec2-terminate-instances $INSTANCE_ID


exit 0
