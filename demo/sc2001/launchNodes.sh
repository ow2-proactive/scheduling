#!/bin/sh

HOSTS=`echo $LSB_HOSTS | /usr/bin/tr [:blank:] "\n" | /bin/sort -u -m`
echo "Hosts = $HOSTS"
echo $HOSTS > hostsList
nodesList=""

# node1
for i in $HOSTS
do
  lsgrun -p -m "$i" launchNode.sh ${i}Node1 &
  nodesList="${nodesList} //$i/${i}Node1"
done
echo $nodesList > nodesList
echo local > localFileTag
sleep 5

# node2
for i in $HOSTS
do
  lsgrun -p -m "$i" launchNode.sh ${i}Node2 &
done

# echo Copying files 
#copyFiles.sh 
     # 2> /dev/null
