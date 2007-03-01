#! /bin/sh

# Get OAR Jobs owned by $USER
function get_oarjobs {
	$SSH_CMD $(get_oar_hostname $1) \
		'oarstat | tail +4 | egrep "^[0-9]" | grep $USER | cut -d" " -f1 '
}

# Delete OAR Jobs owned by $USER
# Reservation ___ARE NOT___ deleted
function clean_oarjobs {
	$SSH_CMD $(get_oar_hostname $1) \
  		'oarstat | tail +4 | egrep "^[0-9]" | grep -v sleep | grep $USER | cut -d" " -f1 | xargs -n1 oardel 2>&1 |grep -v usage '
}

# Delete OAR RESERVATIONS owned by $USER
# Reservation ___ARE___ deleted
function clean_oarreservations {
	$SSH_CMD $(get_oar_hostname $1) \
  		'oarstat | tail +4 | egrep "^[0-9]" | grep sleep | grep $USER | cut -d" " -f1 | xargs -n1 oardel 2>&1 |grep -v usage '
}

# Delete OAR Jobs owned by $USER
# Reservation ___ARE___ deleted
function clean_oarjobs_and_reservations {
	$SSH_CMD $(get_oar_hostname $1) \
  		'oarstat | tail +4 | egrep "^[0-9]"  | grep $USER | cut -d" " -f1 | xargs -n1 oardel 2>&1 |grep -v usage '
}


# Delete OAR logfile (~/OAR.*) on the given site
function clean_oarlogfiles {
 if [ -z $2 ] || [ $2 = $LOCAL_CLUSTER ] ;
 then
  # remove logfile in pwd
  echo rm ~/OAR.*.$1.std*
 else
  # logfile should be in $HONE
  echo $SSH_CMD $(get_frontale_hostname $2) \
  rm ~/OAR.*.$1.std*
  fi
}

function get_nodescount {
	$SSH_CMD $(get_oar_hostname $1) \
		'oarnodes -s | grep -i alive | wc -l'
}

# Get free nodes for the cluster $1
function get_freenodes {
	$SSH_CMD $(get_oar_hostname $1) \
 		'oarnodes | grep "state = free" | wc -l | sed "s/^\s*//"'
}

# Get node statistics for the cluster $1
function get_nodesstats {
	$SSH_CMD $(get_oar_hostname $1) \
'oarnodes | grep state | sort | uniq -c |awk '\'' {print "\t" $4 "     \t" $1} '\'

}

# Get OAR Jobs statistics for the cluster $1 and user $USER
function get_oar_jobs {
	$SSH_CMD $(get_oar_hostname $1) \
		'oarstat  -a | tail +7 | grep $USER | awk '\'' {print "Job: "$1 "\tNodes: "  $6}'\'' | sed '\''s/\([0-9]\+\)\w\+/\1/'\'
}


