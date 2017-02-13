#!/bin/sh
echo $LSB_HOSTS
#HOSTS=`echo $LSB_HOSTS | /usr/bin/tr [:blank:] "\n" | /bin/sort -u -m`
#echo "Hosts = $HOSTS"
#echo $HOSTS > hostsList

ARGS=""
for i in $@
do
    ARGS="$ARGS $i"
done

CMD=$(echo ${ARGS})

nodesList=""

# node1
for i in $LSB_HOSTS
do
  /usr/local/lsf/bin/lsgrun -p -m "$i" $CMD &
#  nodesList="${nodesList} //$i/${i}Node1"
done
#echo $nodesList > nodesList
#echo local > localFileTag
#sleep 5

# node2
#for i in $HOSTS
#do
#  /usr/local/lsf/bin/lsgrun -p -m "$i" $CMD &
#done

# echo Copying files
#copyFiles.sh
     # 2> /dev/null
