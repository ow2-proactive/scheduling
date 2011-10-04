#!/bin/sh

function warn_and_exit {
	echo "$1" 1>&2
	exit 1
}

function warn_print_usage_and_exit {
	echo "$1" 1>&2
	echo "" 1>&2
	echo "Usage: $0 SCHEDULER_DIR VERSION JAVA_HOME" 1>&2
	exit 1
}

function init_env() {
	echo "********************** Initializing environment ************************"
	TMP=/tmp

	# /mnt/scratch is a tmpfs mount point for faster builds on schubby
	if [ -w "/mnt/scratch" ] ; then
		TMP=/mnt/scratch
	fi

	SCHEDULER_DIR=`readlink -f $1`
	VERSION=$2
	JAVA_HOME=$3
	if [ ! -z "$4" ] ; then
		TMP=$4
	fi

	TMP_DIR=""

	echo " [i] SCHEDULER_DIR: $SCHEDULER_DIR"
	echo " [i] VERSION:       $VERSION"
	echo " [i] JAVA_HOME:     $JAVA_HOME"
	echo " [i] TMP:           $TMP"

	if [ -z "$SCHEDULER_DIR" ] ; then
		warn_print_usage_and_exit "SCHEDULER_DIR is not defined"
	fi

	if [ -z "$VERSION" ] ; then
		warn_print_usage_and_exit "VERSION is not defined"
	fi

	if [ -z "$JAVA_HOME" ] ; then
		warn_print_usage_and_exit "JAVA_HOME is not defined"
	fi

	export JAVA_HOME=${JAVA_HOME}

	# name of the directory that contains the full scheduling content  (also set in release-create.sh)
	SCHEDULING_FULL_NAME=Scheduling-${VERSION}_full
}

function copy_to_tmp() {
	echo "********************** Copying the product to tmp dir ************************"
	TMP_DIR="${TMP}/${SCHEDULING_FULL_NAME}"
	output=$(mkdir ${TMP_DIR} 2>&1)
	if [ "$?" -ne 0 ] ; then
		if [ -e ${TMP_DIR} ] ; then
			echo " [w] ${TMP_DIR} already exists. Delete it !"
			rm -rf ${TMP_DIR}
			mkdir ${TMP_DIR}
			if [ "$?" -ne 0 ] ; then
				warn_and_exit "Cannot create ${TMP_DIR}: $output"
			fi
		else
			warn_and_exit "Cannot create ${TMP_DIR}"
		fi
	fi

	echo Copying files to $TMP_DIR
	cp -Rf ${SCHEDULER_DIR}/* ${TMP_DIR}
	cp -Rf ${SCHEDULER_DIR}/.classpath ${TMP_DIR}
	cp -Rf ${SCHEDULER_DIR}/.project ${TMP_DIR}
}

function replace_version() {
	echo "********************** Replacing version ************************"
	cd ${TMP_DIR} || warn_and_exit "Cannot move in ${TMP_DIR}"
	# Replace RCP version pattern with current version
	find scheduler-rcp -type f -name "*" -exec sed -i "s/11\.22\.33/${VERSION}/" {} \;
	find rm-rcp -type f -name "*" -exec sed -i "s/11\.22\.33/${VERSION}/" {} \;

	sed -i "s/{version}/$VERSION/" README.txt
}

function build_and_clean() {
	echo "********************** Cleaning the product ************************"

	cd ${TMP_DIR} || warn_and_exit "Cannot move in ${TMP_DIR}"
	if [ "$(find src/ -name "*.java" | xargs grep serialVersionUID | grep -v `echo $VERSION | sed 's@\(.\)\.\(.\)\..@\1\2@'` | wc -l)" -gt 0 ] ; then
		if [ -z "${RELAX}" ] ; then
			find src/ -name "*.java" | xargs grep serialVersionUID | grep -v `echo $VERSION | sed 's@\(.\)\.\(.\)\..@\1\2@'`
			warn_and_exit " [E] Previous files does not define proper serialVersionUID !"
		fi
	fi

	# Subversion
	find . -type d -a -name ".svn" -exec rm -rf {} \;

	# Remove database directory if exist
	find . -type d -name "SCHEDULER_DB" -exec rm -rf {} \;
	find . -type d -name "RM_DB" -exec rm -rf {} \;
	# Remove logs directory
	rm -rf ${SCHEDULER_DIR}/.logs

	echo "********************** Building the product ***********************"
	# Replace version tag in main java file
	sed -i "s/{srm-version-main}/$VERSION/" src/common/org/ow2/proactive/Main.java
	sed -i "s/{scheduler-version-main}/$VERSION/" src/scheduler/src/org/ow2/proactive/scheduler/common/Main.java
	sed -i "s/{rm-version-main}/$VERSION/" src/resource-manager/src/org/ow2/proactive/resourcemanager/common/Main.java

	cd compile || warn_and_exit "Cannot move in compile"
	./build clean
	./build -Dversion="${VERSION}" deploy.all rcp.build.rm rcp.build.scheduler
	./build -Dversion="${VERSION}" doc.Scheduler.manualPdf doc.rm.manualPdf doc.MapReduce.manualPdf

	echo "********************** Building the product ***********************"
	generate_credential

	cd ${TMP_DIR} || warn_and_exit "Cannot move in ${TMP_DIR}"
	echo " [i] Clean"

	# Git
	rm -rf .git

	# Remove useless parts of ProActive
	find . -type f -a -name "*.svg" -exec rm {} \; # svg are converted in png by hands

	# Remove non GPL stuff
	rm -rf ./compile/lib/clover.*

	# Remove temporary files
	rm compile/junit*properties
	rm -rf classes/
	rm -rf docs/tmp/

	# Remove dev directory
	rm -rf dev/
}

function generate_credential() {
	echo "********************** Generating credential ************************"
	# ../bin/unix/key-gen -P ../config/authentication/keys/pub.key -p ../config/authentication/keys/priv.key
	../bin/unix/create-cred -F ../config/authentication/keys/pub.key -l rm -p rm_pwd -o ../config/authentication/rm.cred
	../bin/unix/create-cred -F ../config/authentication/keys/pub.key -l scheduler -p scheduler_pwd -o ../config/authentication/scheduler.cred
}

function moveRCPs(){
	echo "***************** Move RM and Scheduler RCPs ********************"
	mv ${TMP_DIR}/dist/rm-rcp/{*.zip,*.tar.gz} ${TMP_DIR}/..
	mv ${TMP_DIR}/dist/scheduler-rcp/{*.zip,*.tar.gz} ${TMP_DIR}/..
}


init_env $*
copy_to_tmp
replace_version
build_and_clean
moveRCPs
