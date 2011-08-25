#!/bin/sh
. dev/release/common

function build() {
	# Replace version tag in main java file
	sed -i "s/{srm-version-main}/$VERSION/" src/common/org/ow2/proactive/Main.java
	sed -i "s/{scheduler-version-main}/$VERSION/" src/scheduler/src/org/ow2/proactive/scheduler/common/Main.java
	sed -i "s/{rm-version-main}/$VERSION/" src/resource-manager/src/org/ow2/proactive/resourcemanager/common/Main.java

	echo "********************** Building the scheduler ************************"
	cd compile || warn_and_exit "Cannot move in compile"
	./build clean
	./build -Dversion="${VERSION}" deploy.all
	./build -Dversion="${VERSION}" doc.Scheduler.manualPdf
	./build -Dversion="${VERSION}" doc.rm.manualPdf
	./build -Dversion="${VERSION}" doc.MapReduce.manualPdf

	generate_credential
}

function buildRCPs(){
	cd ${TMP_DIR}/scheduler-rcp/org.ow2.proactive.scheduler.script
	"$JAVA_HOME"/bin/java -jar /user/jlscheef/home/bin/eclipse-3.7-jee/plugins/org.eclipse.equinox.launcher_1.2.0.v20110502.jar -application org.eclipse.ant.core.antRunner
	mv *.zip *.tar.gz ${TMP_DIR}/..
}



init_env Scheduler $*
SERVER_NAME=full
copy_to_tmp
build_and_clean
replace_version
buildRCPs
