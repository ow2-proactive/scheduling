#!/bin/sh

#argument 1 is the temp directory where Scheduling_full directory and RCPs archives must be
TMP_DIR=`readlink -f $1`
#argument 2 is the current version being released
VERSION=$2

#############################################################################

# name of the directory that contains the full scheduling content
SCHEDULING_FULL_NAME=Scheduling-${VERSION}_full

# releases names will be : ${PREFIX}${VERSION}$[SUFFIX}.ext
# and for RCPs :           ${PREFIX}${VERSION}$[SUFFIX}os.type.arch.ext

PREFIX_API_SC=ProActiveScheduling-
PREFIX_API_RM=ProActiveResourcing-
PREFIX_RCP_SC=ProActiveScheduling-
PREFIX_RCP_RM=ProActiveResourcing-
PREFIX_SRC=ProActiveSchedulingResourcing-

SUFFIX_SRC=_src
SUFFIX_SERVER=_server
SUFFIX_WORKER=_worker
SUFFIX_CLIENT_API=_client-API
SUFFIX_CLIENT_API_FULL=_client-API-full
SUFFIX_CLIENT_API_MATSCI=_client-API-matlab-scilab
SUFFIX_CLIENT_API_MAPREDUCE=_client-API-mapreduce

SUFFIX_CLIENT_RCPs=_client-RCP-

#############################################################################
#############################################################################

function del_dist(){
	rm -rf dist
}

function del_src(){
	rm -rf compile lib rm-rcp scheduler-rcp scripts src
	rm -rf doc/src doc/toolchain doc/tmp
}

function del_client(){
	rm -rf compile lib rm-rcp scheduler-rcp scripts src
	rm -rf doc/src doc/toolchain
	rm -rf doc/built/Resourcing
	rm -rf samples/scripts/deployment
	rm RM-README.txt
	rm bin/**/rm* bin/**/*start*
	rm -rf config/authentication/*.cfg config/authentication/jaas.config config/authentication/keys
	rm config/log4j/log4j-defaultNode config/log4j/*server
	rm -rf config/rm config/scheduler
	rm config/security.java.policy-server
	rm dist/lib/*ResourceManager* dist/lib/ProActive_Scheduler-fsm.jar
	rm dist/lib/derby* dist/lib/hibernate-core.jar dist/lib/mysql-connector-java-5.1.16-bin.jar dist/lib/virtual* dist/lib/j-interop*.jar dist/lib/xenserver-5.0.0-3.jar
}

function del_matsci() {
	rm -rf extensions/matlab extensions/scilab
	rm -rf samples/jobs_descriptors/job_scilab
	rm samples/scripts/selection/*lab.xml
	rm dist/lib/*matlab* dist/lib/*scilab* dist/lib/ProActive_LicenseSaver*.jar
	rm dist/lib/ProActive_Scheduler-matsci.jar
}

function del_mapreduce() {
	rm -rf extensions/mapreduce
	rm -rf samples/jobs_descriptors/Workflow/mapreduce
	rm -rf doc/built/MapReduce
	rm dist/lib/hadoop*
	rm dist/lib/ProActive_Scheduler-mapreduce.jar
}

function del_scheduler() {
	#removing all files related to the scheduler
	rm -rf .project
	rm -rf .classpath
	rm -rf README.txt
	rm -rf extensions
	rm -rf samples/jobs_descriptors
	rm -rf samples/munin
	rm -rf doc/built/MapReduce doc/built/Scheduling
	#removing src and lib
	del_src
	del_matsci
	del_mapreduce
	find . -name "scheduler*" -exec rm -rf {} \;
	find . -name "matlab*" -exec rm -rf {} \;
	find . -name "scilab*" -exec rm -rf {} \;
	find . -name "mapreduce*" -exec rm -rf {} \;
	mv RM-README.txt README.txt
}

#############################################################################

function warn_print_usage_and_exit {
	echo "$1" 1>&2
	echo "" 1>&2
	echo "Usage: $0 TMP_DIR VERSION" 1>&2
	echo "       TMP_DIR : directory containing scheduling-full and RCP archives
	echo "       VERSION : current version to be released
	exit 1
}

function cp_r_full(){
	cd $TMP_DIR
	cp -r ${SCHEDULING_FULL_NAME} $1
}

function create_archive(){
	tar cvfz $1.tar.gz $1
	zip -r   $1.zip    $1
	rm -rf $1
}



#                       RUN BABY ! RUN !


if [ -z "$TMP_DIR" ] ; then
	warn_print_usage_and_exit "'TMP_DIR' is not defined"
fi
if [ -z "$VERSION" ] ; then
	warn_print_usage_and_exit "'VERSION' is not defined"
fi


echo "---------------> Building API archives..."

# SchedulingResourcing src ########################
echo "---------------> Creating SchedulingResourcing SRC..."
ARCHIVE_NAME=${PREFIX_SRC}${VERSION}${SUFFIX_SRC}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_dist
cd ..
create_archive ${ARCHIVE_NAME}


# Resourcing server ###############################
echo "---------------> Creating Resourcing SERVER..."
ARCHIVE_NAME=${PREFIX_API_RM}${VERSION}${SUFFIX_SERVER}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_scheduler
cd ..
create_archive ${ARCHIVE_NAME}


# Scheduling server ###############################
echo "---------------> Creating Scheduling SERVER..."
ARCHIVE_NAME=${PREFIX_API_SC}${VERSION}${SUFFIX_SERVER}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_src
cd ..
create_archive ${ARCHIVE_NAME}


# Scheduling worker ###############################
echo "---------------> Creating Scheduling WORKER..."
ARCHIVE_NAME=${PREFIX_API_SC}${VERSION}${SUFFIX_WORKER}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_src
rm -rf samples
rm -rf extensions
rm -rf doc
rm RM-README.txt
rm bin/**/pa-dataserver* bin/**/rm-client* bin/**/rm-start-clean* bin/**/start* bin/unix/rm-start bin/windows/rm-start.bat bin/**/scheduler* bin/**/create-cred* bin/**/key-gen*
rm -rf config/authentication/*.cfg config/authentication/jaas.config config/authentication/keys
rm config/log4j/*server config/log4j/*client
rm -rf config/rm config/scheduler
rm config/security.java.policy-server
del_matsci
del_mapreduce
cd ..
create_archive ${ARCHIVE_NAME}


# Scheduling client API ###########################
echo "---------------> Creating Scheduling client API..."
ARCHIVE_NAME=${PREFIX_API_SC}${VERSION}${SUFFIX_CLIENT_API}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_client
del_matsci
del_mapreduce
cd ..
create_archive ${ARCHIVE_NAME}


# Scheduling client API full ######################
echo "---------------> Creating Scheduling client API full..."
ARCHIVE_NAME=${PREFIX_API_SC}${VERSION}${SUFFIX_CLIENT_API_FULL}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_client
cd ..
create_archive ${ARCHIVE_NAME}


# Scheduling client API matsci ####################
echo "---------------> Creating Scheduling client API matsci..."
ARCHIVE_NAME=${PREFIX_API_SC}${VERSION}${SUFFIX_CLIENT_API_MATSCI}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_client
del_mapreduce
cd ..
create_archive ${ARCHIVE_NAME}


# Scheduling client API mapreduce ################
echo "---------------> Creating Scheduling client API mapreduce..."
ARCHIVE_NAME=${PREFIX_API_SC}${VERSION}${SUFFIX_CLIENT_API_MAPREDUCE}
cp_r_full ${ARCHIVE_NAME}
cd ${ARCHIVE_NAME}
del_client
del_matsci
cd ..
create_archive ${ARCHIVE_NAME}




#                       RCPs

echo "---------------> Building RCP archives..."

cd $TMP_DIR

for i in {Scheduling,Resourcing}*{.tar.gz,zip}
do

	if [ -f $i ] # avoid having bad file as Scheduling*.zip, Scheduling*.tar.gz
	then

		# get RCP type of archive
		if [[ $i == *linux.gtk.x86_64* ]]
		then
			RCP_TYPE=linux.gtk.x86_64
		elif [[ $i == *win32.win32.x86_64* ]]
		then
			RCP_TYPE=win32.win32.x86_64
		elif [[ $i == *linux.gtk.x86* ]]
		then
			RCP_TYPE=linux.gtk.x86
		elif [[ $i == *win32.win32.x86* ]]
		then
			RCP_TYPE=win32.win32.x86
		elif [[ $i == *macosx.cocoa.x86_64* ]]
		then
			RCP_TYPE=macosx.cocoa.x86_64
		fi

		# uncompress current archive
		if [[ $i == *.zip ]]
		then
			unzip $i
		else
			tar xzvf $i
		fi
		# remove old compressed archive
		rm $i

		# rename old archive content directory to new output directory for final archive
		if [[ $i == Scheduling* ]]
		then
			OUTPUT_DIRECTORY=${PREFIX_RCP_SC}${VERSION}${SUFFIX_CLIENT_RCPs}${RCP_TYPE}
			mv Scheduling-${VERSION}-RCP ${OUTPUT_DIRECTORY}
		else
			OUTPUT_DIRECTORY=${PREFIX_RCP_RM}${VERSION}${SUFFIX_CLIENT_RCPs}${RCP_TYPE}
			mv Resourcing-${VERSION}-RCP ${OUTPUT_DIRECTORY}
		fi

		# FROM -> ${SCHEDULING_FULL_NAME}
		# TO   -> ${OUTPUT_DIRECTORY}

		#COPY LICENSE FILES
		cp ${SCHEDULING_FULL_NAME}/LICENSE.txt ${OUTPUT_DIRECTORY}
		cp ${SCHEDULING_FULL_NAME}/LICENSE_EXCEPTION.txt ${OUTPUT_DIRECTORY}


		# bin (copy only needed file + change classpath in env/init.bat)
		mkdir ${OUTPUT_DIRECTORY}/bin
		if [ "${RCP_TYPE}" = "win32.win32.x86" ] || [ "${RCP_TYPE}" = "win32.win32.x86_64" ]
		then
			mkdir ${OUTPUT_DIRECTORY}/bin/windows
			cp ${SCHEDULING_FULL_NAME}/bin/windows/*.bat ${OUTPUT_DIRECTORY}/bin/windows/
			rm ${OUTPUT_DIRECTORY}/bin/windows/start-router.bat
			rm ${OUTPUT_DIRECTORY}/bin/windows/key-gen.bat
			if [[ $i == Scheduling* ]]
			then
				rm ${OUTPUT_DIRECTORY}/bin/windows/rm*
				rm ${OUTPUT_DIRECTORY}/bin/windows/scheduler-start*
				sed -i "s#dist\\\\lib#plugins\\\\org.ow2.proactive.scheduler.lib_${VERSION}\\\\lib#g" ${OUTPUT_DIRECTORY}/bin/windows/init.bat
			else
				rm ${OUTPUT_DIRECTORY}/bin/windows/scheduler*
				rm ${OUTPUT_DIRECTORY}/bin/windows/rm-start.bat
				rm ${OUTPUT_DIRECTORY}/bin/windows/rm-start-clean.bat
				sed -i "s#dist\\\\lib#plugins\\\\org.ow2.proactive.resourcemanager.lib_${VERSION}\\\\lib#g" ${OUTPUT_DIRECTORY}/bin/windows/init.bat
			fi
		else
			mkdir ${OUTPUT_DIRECTORY}/bin/unix
			cp ${SCHEDULING_FULL_NAME}/bin/unix/* ${OUTPUT_DIRECTORY}/bin/unix/
			rm ${OUTPUT_DIRECTORY}/bin/unix/start-router
			rm ${OUTPUT_DIRECTORY}/bin/unix/key-gen
			if [[ $i == Scheduling* ]]
			then
				rm ${OUTPUT_DIRECTORY}/bin/unix/rm*
				rm ${OUTPUT_DIRECTORY}/bin/unix/scheduler-start*
				sed -i "s#dist/lib#plugins/org.ow2.proactive.scheduler.lib_$VERSION/lib#g" ${OUTPUT_DIRECTORY}/bin/unix/env
			else
				rm ${OUTPUT_DIRECTORY}/bin/unix/scheduler*
				rm ${OUTPUT_DIRECTORY}/bin/unix/rm-start
				rm ${OUTPUT_DIRECTORY}/bin/unix/rm-start-clean
				sed -i "s#dist/lib#plugins/org.ow2.proactive.resourcemanager.lib_$VERSION/lib#g" ${OUTPUT_DIRECTORY}/bin/unix/env
			fi
		fi

		# doc  -----------------------------
		mkdir -p ${OUTPUT_DIRECTORY}/doc/built
		if [[ $i == Scheduling* ]]
		then
			cp -r ${SCHEDULING_FULL_NAME}/doc/built/Scheduling ${OUTPUT_DIRECTORY}/doc/built/
		else
			cp -r ${SCHEDULING_FULL_NAME}/doc/built/Resourcing ${OUTPUT_DIRECTORY}/doc/built/
		fi

		# samples -----------------------------
		cp -r ${SCHEDULING_FULL_NAME}/samples ${OUTPUT_DIRECTORY}
		rm -r ${OUTPUT_DIRECTORY}/samples/munin
		if [[ $i == Resourcing* ]]
		then
			rm -r ${OUTPUT_DIRECTORY}/samples/jobs_descriptors
		fi

		# config -----------------------------
		mkdir -p ${OUTPUT_DIRECTORY}/config/log4j
		cp ${SCHEDULING_FULL_NAME}/config/log4j/log4j-client ${OUTPUT_DIRECTORY}/config/log4j/
		cp -r ${SCHEDULING_FULL_NAME}/config/proactive ${OUTPUT_DIRECTORY}/config/
		cp ${SCHEDULING_FULL_NAME}/config/security.java.policy-client ${OUTPUT_DIRECTORY}/config/
		if [[ $i == Resourcing* ]]
		then
			mkdir ${OUTPUT_DIRECTORY}/config/rm
			cp -r ${SCHEDULING_FULL_NAME}/config/rm/deployment ${OUTPUT_DIRECTORY}/config/rm
			rm ${OUTPUT_DIRECTORY}/config/rm/deployment/GCMNodeSourceApplication.xml
		fi

		# add ProActive configuration reference in product.ini
		if [[ $i == Scheduling* ]]
		then
			INI_FILE=Scheduler.ini
		else
			INI_FILE=ResourceManager.ini
		fi
		if [ "${RCP_TYPE}" = "macosx.cocoa.x86_64" ] || [ "${RCP_TYPE}" = "macosx.carbon.ppc" ]
	    then
			echo "-Dproactive.configuration=../../../config/proactive/ProActiveConfiguration.xml" >> ${OUTPUT_DIRECTORY}/$PRODUCT_NAME.app/Contents/MacOS/$INI_FILE
	    else
			echo "-Dproactive.configuration=config/proactive/ProActiveConfiguration.xml" >> ${OUTPUT_DIRECTORY}/$INI_FILE
	    fi

	    # add addons directory
	    if [ -d "${SCHEDULING_FULL_NAME}/addons" ]
		then
			cp -r ${SCHEDULING_FULL_NAME}/addons ${OUTPUT_DIRECTORY}
		else
			mkdir -p ${OUTPUT_DIRECTORY}/addons
		fi

		# add extensions directory
		cp -r ${SCHEDULING_FULL_NAME}/extensions ${OUTPUT_DIRECTORY}


		# re-create archive
		if [[ $i == *.zip ]]
		then
			# zip created directory
			zip -r ${OUTPUT_DIRECTORY}.zip ${OUTPUT_DIRECTORY}
		else
			# tar created directory
			tar zcvf ${OUTPUT_DIRECTORY}.tar.gz ${OUTPUT_DIRECTORY}
		fi

		# clean output directory
		rm -rf ${OUTPUT_DIRECTORY}

	fi

done
