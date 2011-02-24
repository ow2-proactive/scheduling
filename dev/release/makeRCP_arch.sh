#!/bin/sh

MAKE_CLIENT_RELEASE=true

if [ $# -ne 4 ]
then
    echo usage : $0 root_directory RCPs_directory version destination
	echo
	echo "    root_directory   : Root directory of the Scheduling project : must contains license files"
	echo "    RCPs_directory   : Directory containing the different built RCPs for both products (must contain the \'scheduler\' and \'rm\' directories)"
	echo "    version          : Version number to release"
	echo "    destination      : Destination path for the final generated archive"
	echo
	echo "    Example :"
	echo "    ./makeRCP_arch.sh /tmp/ProActiveScheduling-1.0.0_server /home/Public/RCP 1.0.0 /home/Public/ProActiveScheduling-1.0.0"
	echo "    "
	echo "    In product export wizard :"
	echo "    To export plugins, Root directory must be :"
	echo "       'ResourceManager'    for RM"
	echo "       'Scheduler'          for Scheduler"
	echo "    Generated plugins directories must be :"
	echo "       'RCPs_directory/rm'          for RM"
    echo "       'RCPs_directory/scheduler'   for Scheduler"
	exit
fi

function generate
{
ROOT_DIRECTORY=$1
RCPs_DIRECTORY=$2/$PRODUCT
VERSION=$3
DESTINATION=$4

# CHECK ROOT DIRECTORY ARGUMENT
if [ ! -d "$ROOT_DIRECTORY" ]
then
	echo \'$ROOT_DIRECTORY\' is not a valid directory
	exit
fi

# CHECK PRODUCT NAME ARGUMENT
if [ "$PRODUCT" = "scheduler" ]
then
	PREFIX=ProActiveScheduling-
	PRODUCT_NAME=Scheduler
elif [ "$PRODUCT" = "rm" ]
then
	PREFIX=ProActiveResourcing-
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

	# PREPARE SUFFIXES
    if [ "$FILENAME" = "linux.gtk.x86" ]
    then
            SUFFIX=linux
    fi
    if [ "$FILENAME" = "linux.gtk.x86_64" ]
    then
            SUFFIX=linux-64
    fi
    if [ "$FILENAME" = "win32.win32.x86" ]
    then
            SUFFIX=win32
    fi
    if [ "$FILENAME" = "win32.win32.x86_64" ]
    then
            SUFFIX=win32-64
    fi
    if [ "$FILENAME" = "macosx.carbon.x86" ]
    then
            SUFFIX=macx86
    fi
    if [ "$FILENAME" = "macosx.carbon.ppc" ]
    then
            SUFFIX=maccarbon
    fi


	#COPY LICENSE FILES
	cp $ROOT_DIRECTORY/LICENSE.txt $i/$PRODUCT_NAME/
	cp $ROOT_DIRECTORY/LICENSE_EXCEPTION.txt $i/$PRODUCT_NAME/


	# GENERATE NEW DIRECTORY NAME
	NEWFILE=${PREFIX}${VERSION}_client-${SUFFIX}
	# COPY DIRECTORY CONTAINING RCP LAUNCHER TO RCP ROOT DIRECTORY
	NEW_FILE_DIR=${RCPs_DIRECTORY}/$NEWFILE
	cp -r $i/$PRODUCT_NAME $NEW_FILE_DIR


	#CREATE CLIENT RELEASE IF NEEDED
	if [ "$MAKE_CLIENT_RELEASE" = "true" ]
	then
		# bin (copy only needed file + change classpath in env/init.bat)
		mkdir $NEW_FILE_DIR/bin
		if [ "$FILENAME" = "win32.win32.x86" ] || [ "$FILENAME" = "win32.win32.x86_64" ]
		then
			mkdir $NEW_FILE_DIR/bin/windows
			cp $ROOT_DIRECTORY/bin/windows/*.bat $NEW_FILE_DIR/bin/windows/
			rm $NEW_FILE_DIR/bin/windows/start-router.bat
			rm $NEW_FILE_DIR/bin/windows/key-gen.bat
			if [ "$PRODUCT" = "scheduler" ]
			then
				rm $NEW_FILE_DIR/bin/windows/rm*
				rm $NEW_FILE_DIR/bin/windows/scheduler-start*
				sed -i "s#dist\\\\lib#plugins\\\\org.ow2.proactive.scheduler.lib_$VERSION\\\\lib#g" $NEW_FILE_DIR/bin/windows/init.bat
			elif [ "$PRODUCT" = "rm" ]
			then
				rm $NEW_FILE_DIR/bin/windows/scheduler*
				rm $NEW_FILE_DIR/bin/windows/rm-start.bat
				rm $NEW_FILE_DIR/bin/windows/rm-start-clean.bat
				sed -i "s#dist\\\\lib#plugins\\\\org.ow2.proactive.resourcemanager.lib_$VERSION\\\\lib#g" $NEW_FILE_DIR/bin/windows/init.bat
			fi
		else
			mkdir $NEW_FILE_DIR/bin/unix
			cp $ROOT_DIRECTORY/bin/unix/* $NEW_FILE_DIR/bin/unix/
			rm $NEW_FILE_DIR/bin/unix/start-router
			rm $NEW_FILE_DIR/bin/unix/key-gen
			if [ "$PRODUCT" = "scheduler" ]
			then
				rm $NEW_FILE_DIR/bin/unix/rm*
				rm $NEW_FILE_DIR/bin/unix/scheduler-start*
				sed -i "s#dist/lib#plugins/org.ow2.proactive.scheduler.lib_$VERSION/lib#g" $NEW_FILE_DIR/bin/unix/env
			elif [ "$PRODUCT" = "rm" ]
			then
				rm $NEW_FILE_DIR/bin/unix/scheduler*
				rm $NEW_FILE_DIR/bin/unix/rm-start
				rm $NEW_FILE_DIR/bin/unix/rm-start-clean
				sed -i "s#dist/lib#plugins/org.ow2.proactive.resourcemanager.lib_$VERSION/lib#g" $NEW_FILE_DIR/bin/unix/env
			fi
		fi

		# doc  -----------------------------
		mkdir -p $NEW_FILE_DIR/doc/built
		if [ "$PRODUCT" = "scheduler" ]
		then
			cp -r $ROOT_DIRECTORY/doc/built/Scheduling $NEW_FILE_DIR/doc/built/
		elif [ "$PRODUCT" = "rm" ]
		then
			cp -r $ROOT_DIRECTORY/doc/built/Resourcing $NEW_FILE_DIR/doc/built/
		fi

		# samples -----------------------------
		cp -r $ROOT_DIRECTORY/samples $NEW_FILE_DIR/
		rm -r $NEW_FILE_DIR/samples/munin
		if [ "$PRODUCT" = "rm" ]
		then
			rm -r $NEW_FILE_DIR/samples/jobs_descriptors
		fi

		# config -----------------------------
		mkdir -p $NEW_FILE_DIR/config/log4j
		cp $ROOT_DIRECTORY/config/log4j/log4j-client $NEW_FILE_DIR/config/log4j/
		cp -r $ROOT_DIRECTORY/config/proactive $NEW_FILE_DIR/config/
		cp $ROOT_DIRECTORY/config/security.java.policy-client $NEW_FILE_DIR/config/
		if [ "$PRODUCT" = "rm" ]
		then
			mkdir $NEW_FILE_DIR/config/rm
			cp -r $ROOT_DIRECTORY/config/rm/deployment $NEW_FILE_DIR/config/rm
			rm $NEW_FILE_DIR/config/rm/deployment/GCMNodeSourceApplication.xml
		fi

		# add ProActive configuration reference in product.ini
		if [ "$PRODUCT" = "scheduler" ]
		then
			INI_FILE=Scheduler.ini
		elif [ "$PRODUCT" = "rm" ]
		then
			INI_FILE=ResourceManager.ini
		fi
		if [ "$FILENAME" = "macosx.carbon.x86" ] || [ "$FILENAME" = "macosx.carbon.ppc" ]
        then
			echo -Dproactive.configuration=../../../config/proactive/ProActiveConfiguration.xml >> $NEW_FILE_DIR/$PRODUCT_NAME.app/Contents/MacOS/$INI_FILE
        else
			echo -Dproactive.configuration=config/proactive/ProActiveConfiguration.xml >> $NEW_FILE_DIR/$INI_FILE
        fi
        
        # add addons directory
        if [ -d "$ROOT_DIRECTORY/addons" ]
		then
			cp -r $ROOT_DIRECTORY/addons $NEW_FILE_DIR/
		else
			mkdir -p $NEW_FILE_DIR/addons
		fi
		
		# add extensions directory
		cp -r $ROOT_DIRECTORY/extensions $NEW_FILE_DIR/

	fi


	# create archive
	if [ "$FILENAME" = "win32.win32.x86" ] || [ "$FILENAME" = "win32.win32.x86_64" ]
	then
		# ZIP CREATED DIRECTORY
		cd ${RCPs_DIRECTORY}
		zip -r $NEWFILE.zip $NEWFILE
		cd -
		# MOVE GENERATED ZIP TO DESTINATION DIR
		mv $NEW_FILE_DIR.zip $DESTINATION
	else
		# TAR CREATED DIRECTORY
		tar zcvf $NEW_FILE_DIR.tar.gz -C ${RCPs_DIRECTORY} $NEWFILE
		# MOVE GENERATED TAR TO DESTINATION DIR
		mv $NEW_FILE_DIR.tar.gz $DESTINATION
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


