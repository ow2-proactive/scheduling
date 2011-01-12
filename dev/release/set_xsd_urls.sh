SCHEDULER_DIR=$1
VERSION=$2

# to avoid replacement in this file
PREVIOUS_TAG=dev

# mv to scheduler dir
cd $SCHEDULER_DIR
WORKING_DIR=$PWD

# Replace all '$PREVIOUS_TAG' version number by this version number in every XML, XSD, RNC files
echo Replacing \'$PREVIOUS_TAG\' tag with current version for XML files

# Do not replace in .* dirs (like .git...)
for sd in $(ls);
do
find ./$sd -type f -exec sed -i "s#urn:proactive:jobdescriptor:$PREVIOUS_TAG#urn:proactive:jobdescriptor:$VERSION#g" {} \;
find ./$sd -type f -exec sed -i "s#http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/$PREVIOUS_TAG/schedulerjob.xsd#http://www.activeeon.com/public_content/schemas/proactive/jobdescriptor/$VERSION/schedulerjob.xsd#g" {} \;
#find ./$sd -type f -exec sed -i "s#org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$PREVIOUS_TAG/schedulerjob.xsd#org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$VERSION/schedulerjob.xsd#g" {} \;
#find ./$sd -type f -exec sed -i "s#/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$PREVIOUS_TAG/schedulerjob#/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$VERSION/schedulerjob#g" {} \;
find ./$sd -type f -exec sed -i "s#/common/xml/schemas/jobdescriptor/$PREVIOUS_TAG#/common/xml/schemas/jobdescriptor/$VERSION#g" {} \;
done

# create local dir for schemas
cd ./src/scheduler/src/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/
mkdir $VERSION
cp ./$PREVIOUS_TAG/* ./$VERSION/
rm -r ./$PREVIOUS_TAG/*


cd $WORKING_DIR

# Update the website with new schema version
echo convert schema
cd compile
./build convertSchemas
cd ..
echo Update the website with new schema version
#COMACTIVEEON_USER=www
#COMACTIVEEON_LOCALURI=www/public_content/schemas/proactive/jobdescriptor
#ssh $COMACTIVEEON_USER@activeeon.com mkdir $COMACTIVEEON_LOCALURI/$VERSION
#scp src/scheduler/src/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$VERSION/schedulerjob.xsd $COMACTIVEEON_USER@activeeon.com:$COMACTIVEEON_LOCALURI/$VERSION/schedulerjob.xsd
#ssh sea.inria.fr chmod 555 $COMACTIVEEON_LOCALURI/$VERSION
#ssh sea.inria.fr chmod 444 $COMACTIVEEON_LOCALURI/$VERSION/schedulerjob.xsd
