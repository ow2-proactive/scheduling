#!/bin/bash

# synchronizateur de fichiers pour grid5000
# Arnaud Contes v 0.1

MATCH_CLUSTER_HOST=

cluster_name=(Orsay Rennes Lyon Toulouse Grenoble Sophia)
real_host=(gdx0160.orsay.grid5000.fr oar.rennes.grid5000.fr
frontale.lyon.grid5000.fr oar.toulouse.grid5000.fr oar.grenoble.grid5000.fr
sophia.grid5000.inria.fr)


if [ $# -lt 2 ]
then
echo "Grid5000 file synchronizator"
echo "usage $0 <dir to sync from \$HOME> <remote cluster or DNS>"
echo "already known cluster : ${cluster_name[*]} or all"

exit 0
fi


_match_name_host ()
{
#1 is cluster name
 i=0
 while [ "$i" -lt  "${#cluster_name[*]}" ]
 do
#   echo $i : ${cluster_name[$i]}
   if [ "$1" = "${cluster_name[$i]}" ]
   then
#   echo " >>>>>>   $1" = "${cluster_name[$i]}"
   MATCH_CLUSTER_HOST=${real_host[$i]}

   return 0
   fi
 let " i += 1"
 done
 MATCH_CLUSTER_HOST=""
 return 1
}






# rsync on one cluster
_one_rsync ()
{
# $1 path
# $2 cluster host
_match_name_host $2
_rsync $1 $MATCH_CLUSTER_HOST:$1
}

_rsync () {

# test if dirname ends with a /
#if yes we have to remove it
END_SLASH=` echo $1 | sed -e 's:.*/::g' `
THE_PATH="$1"
#if [ -z $END_SLASH ]
#then
#  THE_PATH=${THE_PATH%%/}
#fi


echo " ================= $2 ===================" >> ~/.synchronize.log
rsync -avz --rsh="ssh" "$THE_PATH" "$2" 2>&1 >> ~/.synchronize.log
echo " =================  END ===================" >> ~/.synchronize.log

echo -n "rsync to $2 : "
if [ $? -eq 0 ]
then
echo OK
else
echo FAILED
fi

}

_rsync_all () {
# $1 dir to sync
 for i in ${cluster_name[@]}
 do
   _one_rsync $1 $i
 done
}

if [ -f ~/.synchronize.log ]
then
rm ~/.synchronize.log
touch ~/.synchronize.log
fi

if [ "$2" = "all" ]
then
 _rsync_all $1
else
 _match_name_host $2
#echo "match $2 <-> $MATCH_CLUSTER_HOST"
 if [ -z $MATCH_CLUSTER_HOST ]
 then
   _rsync $1 $2
 else
 _one_rsync $1 $2
 fi
fi

#_one_rsync $1 ${real_host[$2]}
