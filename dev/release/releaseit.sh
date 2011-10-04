#!/bin/sh

# Call this script to build the release once RCPs have been built first.
# This script calls dev/release/release-rm, dev/release/release-all, and makeRCP_arch.sh scripts to completly build the whole release.
# At the end, full Scheduling release can be found in 'destination' directory argument.

#usage if args number < 4
if [ $# -lt 4 ]
then
    echo usage : $0 root_directory RCPs_directory version destination [java_home]
	echo
	echo "    root_directory   : Root directory of the Scheduling project to be released : directory that contains license files"
	echo "    RCPs_directory   : Directory containing the different built RCPs for both products (must contain the 'scheduler' and 'rm' directories)"
	echo "    version          : Version number to release"
	echo "    destination      : Destination path for the final generated archive"
	echo "    java_home        : java_home path (optional - default will be '$JAVA_HOME')"
	echo
	echo "    Example :"
	echo "    $0 /home/Workspace/ProActiveScheduling /home/Public/RCP 1.0.0 /home/Public/ProActiveScheduling-1.0.0"
	echo "    "
	echo "    In Eclipse-delta-pack product export wizard :"
	echo "    To export plugins, Root directory must be :"
	echo "       'ResourceManager'    for RM"
	echo "       'Scheduler'          for Scheduler"
	echo "    Generated plugins directories must be :"
	echo "       'RCPs_directory/rm'          for RM"
    	echo "       'RCPs_directory/scheduler'   for Scheduler"
	echo
	echo
	echo "    Note : To have a special archive name that is not only the version number to release, just set the sysenv var PAS_RELEASE_NAME"
	echo "           with the name which will replace version number is the archive name"
	exit
fi


#assign arguments
ROOT_DIRECTORY=$1
RCPs_DIRECTORY=$2
VERSION=$3
OUTPUT_DIRECTORY=$4
if [ "$#" -eq "5" ]
then
	JAVA_HOME_u=$5
else
	JAVA_HOME_u=$JAVA_HOME
fi


# CHECK ROOT DIRECTORY ARGUMENT
if [ ! -d "$ROOT_DIRECTORY" ] && [ ! -e "$ROOT_DIRECTORY/LICENSE.txt" ]
then
	echo "'$ROOT_DIRECTORY' is not a valid Scheduling root directory"
	exit
fi


#ask user if ready 
echo "*"
echo "*  Release is now ready to be built"
echo "*  Please, ensure that RCPs have been produced with these constraints :"
echo "*     - version pattern 11,22,33 has been replaced with version $VERSION"
echo "*     - resource manager product has been built without Scheduling dependences in its required plug-ins dependences."
echo "*       (org.ow2.proactive.scheduler.lib should not appear in resource_manager.product dependencies)"
echo "*     - $RCPs_DIRECTORY must contain 'rm' and 'scheduler' directories and each one must contain those directories :"
echo "*         - linux.gtk.x86"
echo "*         - linux.gtk.x86_64"
echo "*         - macosx.cocoa.x86_64"
echo "*         - win32.win32.x86"
echo "*         - win32.win32.x86_64"
echo "*"
echo "*   Note : To have a special archive name that is not only the version number to release, just export the sysenv var 'PAS_RELEASE_NAME'"
echo "*          with the name which will replace version number is the archive name"
echo "*"
echo "*  Read $ROOT_DIRECTORY/dev/release/HOWTO_ProActiveScheduling.txt for more details about the release process."
echo "*  Read it now ? (y/n)."
read answer
#check answer, if no 'y' -> exit
if [ "$answer" == "y" ]
then 
	echo "--------------------"
	more $ROOT_DIRECTORY/dev/release/HOWTO_ProActiveScheduling.txt
	echo "--------------------"
fi
echo " "
echo "Are you sure you want to build the release now ? (y/n)"
read answer
#check answer, if no 'y' -> exit
if [ "$answer" != "y" ]
then 
	echo "Aborting... Nothing was done"
	exit
fi


#RELEASE IT -------------------------------
#go into scheduling root dir
cd $ROOT_DIRECTORY
#release RESOURCING first, then SCHEDULING
echo "---------------> 1. Release-rm"
dev/release/release-rm . $VERSION $JAVA_HOME_u
echo "---------------> 2. Release-all"
dev/release/release-all . $VERSION $JAVA_HOME_u
#move create archive in destination dir
echo "---------------> 3. Move rm and scheduler server archive to '$OUTPUT_DIRECTORY'"
SPECIAL_NAME=$VERSION
if [ "$PAS_RELEASE_NAME" != "" ]
then
	SPECIAL_NAME=$PAS_RELEASE_NAME
fi
mv /tmp/ProActiveResourcing-${SPECIAL_NAME}_*.tar.gz $OUTPUT_DIRECTORY
mv /tmp/ProActiveResourcing-${SPECIAL_NAME}_*.zip $OUTPUT_DIRECTORY
mv /tmp/ProActiveScheduling-${SPECIAL_NAME}_*.tar.gz $OUTPUT_DIRECTORY
mv /tmp/ProActiveScheduling-${SPECIAL_NAME}_*.zip $OUTPUT_DIRECTORY
#change dir to dev/release
echo "---------------> 4. Change directory to dev/release"
cd dev/release;
#update RCPs content (add scripts, update launcher init, etc...)
echo "---------------> 5. Update RCPs content"
makeRCP_arch.sh /tmp/ProActiveScheduling-${SPECIAL_NAME}_server $RCPs_DIRECTORY ${VERSION} $OUTPUT_DIRECTORY
#remove remaining temporary server directories
echo "---------------> 6. Remove remaining temporary directories ? y/n"
echo "                       /tmp/ProActiveResourcing-${SPECIAL_NAME}_*"
echo "                       /tmp/ProActiveScheduling-${SPECIAL_NAME}_*"
read answer
#check answer, if 'y' -> remove
if [ "$answer" == "y" ]
then 
    rm /tmp/ProActiveResourcing-${SPECIAL_NAME}_* -rf /tmp/ProActiveScheduling-${SPECIAL_NAME}_*
fi
echo "---------------> 7. End of release process"

