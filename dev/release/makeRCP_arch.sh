#!/bin/sh

# cmd example
# ./makeRCP_arch.sh ../Public/RCP 1.0.2 ../Workspace/ProActiveScheduling.git/scheduler_plugins/org.ow2.proactive.scheduler/proactive-log4j ../Public/RCP

if [ $# -ne 4 ]
then
        echo usage : $0 RCPs_directory version proactive-log4j destination
	echo
	echo "    RCPs_directory   : Directory containing the different built RCPs for both products (must contain the \'scheduler\' and \'rm\' directories)"
	echo "    version          : Version number to release"
	echo "    proactive-log4j  : Path to the proactive-log4j FILE to copy into the RCPs"
	echo "    destination      : Destination path for the final generated archive"
	echo
	echo "    Example :"
	echo "    ./makeRCP_arch.sh ../Public/RCP 1.0.2 ../Workspace/ProActiveScheduling.git/scheduler_plugins/org.ow2.proactive.scheduler/proactive-log4j ../Public/RCP"
	exit
fi

function generate
{
RCPs_DIRECTORY=$1/$PRODUCT
VERSION=$2
LOG4J=$3
DESTINATION=$4

# CHECK PRODUCT NAME ARGUMENT
if [ "$PRODUCT" = "scheduler" ]
then
	PREFIX=ProActiveScheduling-
	PRODUCT_NAME=Scheduler
elif [ "$PRODUCT" = "rm" ]
then
	PREFIX=ProActiveResourceManager-
	PRODUCT_NAME=ResourceManager
else
	echo unknown product name : $PRODUCT
	exit
fi

# CHECK RCPs DIRECTORY ARGUMENT
if [ ! -d "$RCPs_DIRECTORY" ]
then
	echo \'$RCPs_DIRECTORY\' is not a valid directory
	exit
fi

# CHECK LOG4J FILE
if [ ! -e "$LOG4J" ]
then
	echo $LOG4J cannot be found
	exit
fi

# CHECK RCPs DESTINATION DIRECTORY ARGUMENT
if [ ! -d "$DESTINATION" ]
then
	echo \'$DESTINATION\' is not a valid directory
	exit
fi

# FOR EACH RCP PRODUCT PLATFORM
for i in $RCPs_DIRECTORY/*
do
	FILENAME=`basename $i`

	# PREPARE SUFFIXES AND COPY LOG4J FILE IN EACH CASE
	if [ "$FILENAME" = "linux.gtk.x86" ]
	then
		SUFFIX=linux
		cp $LOG4J $i/$PRODUCT_NAME/proactive-log4j
	fi
	if [ "$FILENAME" = "linux.gtk.x86_64" ]
	then
		SUFFIX=linux-64
		cp $LOG4J $i/$PRODUCT_NAME/proactive-log4j
	fi
	if [ "$FILENAME" = "win32.win32.x86" ]
	then
		SUFFIX=win32
		cp $LOG4J $i/$PRODUCT_NAME/proactive-log4j
	fi
	if [ "$FILENAME" = "macosx.carbon.x86" ]
	then
		SUFFIX=macx86
		cp $LOG4J $i/$PRODUCT_NAME/$PRODUCT_NAME.app/Contents/MacOS/proactive-log4j
	fi
	if [ "$FILENAME" = "macosx.carbon.ppc" ]
	then
		SUFFIX=maccarbon
		cp $LOG4J $i/$PRODUCT_NAME/$PRODUCT_NAME.app/Contents/MacOS/proactive-log4j
	fi

	# GENERATE NEW DIRECTORY NAME
	NEWFILE=${PREFIX}${VERSION}_RCP-client-${SUFFIX}
	# COPY DIRECTORY CONTAINING RCP LAUNCHER TO RCP ROOT DIRECTORY
	cp -r $i/$PRODUCT_NAME ${RCPs_DIRECTORY}/$NEWFILE

	if [ "$FILENAME" = "win32.win32.x86" ]
	then
		# ZIP CREATED DIRECTORY
		cd ${RCPs_DIRECTORY}
		zip -r $NEWFILE.zip $NEWFILE
		cd -
		# MOVE GENERATED ZIP TO DESTINATION DIR
		mv ${RCPs_DIRECTORY}/$NEWFILE.zip $DESTINATION
	else
		# TAR CREATED DIRECTORY
		tar zcvf ${RCPs_DIRECTORY}/$NEWFILE.tar.gz -C ${RCPs_DIRECTORY} $NEWFILE
		# MOVE GENERATED TAR TO DESTINATION DIR
		mv ${RCPs_DIRECTORY}/$NEWFILE.tar.gz $DESTINATION
	fi

	# CLEAN DIRECTORY
	rm -rf ${RCPs_DIRECTORY}/$NEWFILE

done
}

for p in {"scheduler","rm"}
do
	PRODUCT=$p
	generate $1 $2 $3 $4
done


