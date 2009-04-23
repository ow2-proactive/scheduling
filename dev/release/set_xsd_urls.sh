SCHEDULER_DIR=$1
VERSION=$2

# to avoid replacement in this file
PREVIOUS_TAG=dev

# mv to scheduler dir
cd $SCHEDULER_DIR
WORKING_DIR=$PWD

# Replace all 'dev' version number by this version number in every XML, XSD, RNC files
echo Replacing \'$PREVIOUS_TAG\' tag with current version for XML files

# Do not replace in .* dirs (like .git...)
for sd in $(ls); 
do
find ./$sd -type f -exec sed -i "s#urn:proactive:jobdescriptor:$PREVIOUS_TAG#urn:proactive:jobdescriptor:$VERSION#g" {} \;
find ./$sd -type f -exec sed -i "s#http://proactive.inria.fr/schemas/jobdescriptor/$PREVIOUS_TAG/schedulerjob.xsd#http://proactive.inria.fr/schemas/jobdescriptor/$VERSION/schedulerjob.xsd#g" {} \;
#find ./$sd -type f -exec sed -i "s#org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$PREVIOUS_TAG/schedulerjob.xsd#org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$VERSION/schedulerjob.xsd#g" {} \;
#find ./$sd -type f -exec sed -i "s#/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$PREVIOUS_TAG/schedulerjob#/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$VERSION/schedulerjob#g" {} \;
find ./$sd -type f -exec sed -i "s#/common/xml/schemas/jobdescriptor/$PREVIOUS_TAG#/common/xml/schemas/jobdescriptor/$VERSION#g" {} \;
done

# create local dir for schemas
cd ./src/scheduler/src/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/
mkdir $VERSION
cp ./dev/* ./$VERSION/
rm -r ./dev/*


cd $WORKING_DIR

# Update the website with new schema version
echo convert schema
./build convertSchemas
echo Update the website with new schema version
ssh sea.inria.fr mkdir /net/servers/www-sop/teams/oasis/proactive/schemas/jobdescriptor/$VERSION
scp src/scheduler/src/org/ow2/proactive/scheduler/common/xml/schemas/jobdescriptor/$VERSION/schedulerjob.xsd $USER@sea.inria.fr:/net/servers/www-sop/teams/oasis/proactive/schemas/jobdescriptor/$VERSION/schedulerjob.xsd
ssh sea.inria.fr chmod 555 /net/servers/www-sop/teams/oasis/proactive/schemas/jobdescriptor/$VERSION
ssh sea.inria.fr chmod 444 /net/servers/www-sop/teams/oasis/proactive/schemas/jobdescriptor/$VERSION/schedulerjob.xsd
