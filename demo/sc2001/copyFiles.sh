#!/bin/sh
#if [  -e "localFileTag" ]
#then
base=/ProActive/demo/sc2001
   echo "
		Copying nodesList and hostsList remotely...
	"
	sub=/net/home/dcaromel/ProActive/demo/sc2001
	for h in A00{5,6}.R868.showfloor.sc2001.org; do
# echo    ---  $HOME/$base/{hostsList,nodesList} dcaromel@$h:$sub
	    rcp $HOME/$base/{hostsList,nodesList} dcaromel@$h:$sub
	done
	sub=/net/home/fhuet/ProActive/demo/sc2001

rcp $HOME/$base/{hostsList,nodesList} fhuet@A006.R868.showfloor.sc2001.org:$sub
rcp $HOME/$base/{hostsList,nodesList} fhuet@A005.R868.showfloor.sc2001.org:$sub
        
#	for h in A00{5,6}.R868.showfloor.sc2001.org; do
# echo  +++ $HOME/$base/{hostsList,nodesList} "fhuet@$h:$sub"
#	    rcp $HOME/$base/{hostsList,nodesList} "fhuet@$h:$sub"
#	done

# else
#   echo "
#		Found nodesList and hostsList locally...
#	"
# fi



