
# Sites participating to Grid5000
SITES=(
	bordeaux
	grenoble
	lille
	lyon
	nancy
	orsay
	rennes
	sophia
	toulouse
)

# Available clusters
CLUSTERS=(
	bordeaux
	idpot.grenoble
	icluster2.grenoble
	lille
	lyon
	nancy
	orsay
	paraci.rennes
	parasol.rennes
	paravent.rennes
	tartopom.rennes
	azur.sophia
	helios.sophia
	toulouse
)

CMD_HOSTNAME=''
if [ "$UNAME" == "Linux" ] ;
then
	CMD_HOSTNAME='hostname -f'
elif [ "$UNAME" == "Darwin" ] ;
then
	CMD_HOSTNAME='hostname'
else
	echo $ERR_PREFIX "cannot set CMD_HOSTNAME, unknow system detected: $UNAME. Aborting" 1>&2
	exit 1
fi



# Return the cluster on which the script is executed
function get_cluster {
	FQDN=`$CMD_HOSTNAME`

	function bordeaux_get_cluster {
		FQDN=$1
		if [ "$FQDN" == "frontale.bordeaux.grid5000.fr" ] ;
		then
			echo ""
		elif [ "$(expr match $FQDN '\(node-[0-9]\{1,2\}.bordeaux.grid5000.fr\)')" == "$FQDN" ] ;
		then
			echo ""
		else
			echo ERR_PREFIX "bordeaux_get_cluster, Strange FQDN=$FQDN for bordeaux site. Aborting" 1>&2
			exit 1
		fi
	}

	function grenoble_get_cluster {
		FQDN=$1
		if [ "$FQDN" == "oar.grenoble.grid5000.fr" ] ;
		then
			echo "idpot"
		elif [ "$(expr match $FQDN '\(idpot[0-9]\{1,3\}.imag.fr\)')" == "$FQDN" ] ;
		then
			echo "idpot"
		elif [ "$(expr match $FQDN '\(ita[0-9]\{0,3\}.imag.fr\)')" == "$FQDN" ] ;
		then
			echo "icluster2"
		else
			echo ERR_PREFIX "bordeaux_get_cluster, Strange FQDN=$FQDN for bordeaux site. Aborting" 1>&2
			exit 1
		fi

	}

	function lille_get_cluster {
		FQDN=$1
		if [ "$FQDN" == "fadmin.lille.grid5000.fr" ] ;
		then
			echo ""
		elif [ "$(expr match $FQDN '\(node-[0-9]\{1,2\}.lille.grid5000.fr\)')" == "$FQDN" ] ;
		then
			echo ""
		else
			echo ERR_PREFIX "lille_get_cluster, Strange FQDN=$FQDN for lille site. Aborting" 1>&2
			exit 1
		fi
	}

	function lyon_get_cluster {
		FQDN=$1
		if [ "$FQDN" == "capricorne.lyon.grid5000.fr" ] ;
		then
			echo ""
		elif [ "$(expr match $FQDN '\(node-[0-9]\{1,2\}.lyon.grid5000.fr\)')" == "$FQDN" ] ;
		then
			echo ""
		else
			echo ERR_PREFIX "lyon_get_cluster, Strange FQDN=$FQDN for lyon site. Aborting" 1>&2
			exit 1
		fi
	}

	function nancy_get_cluster {
		FQDN=$1
		if [ "$FQDN" == "fgrillon1.nancy.grid5000.fr" ] ;
		then
			echo ""
		elif [ "$(expr match $FQDN '\(grillon-[0-9]\{1,2\}.nancy.grid5000.fr\)')" == "$FQDN" ] ;
		then
			echo ""
		else
			echo ERR_PREFIX "nancy_get_cluster, Strange FQDN=$FQDN for nancy site. Aborting" 1>&2
			exit 1
		fi
	}

	function orsay_get_cluster {
		FQDN=$1
		if [ "$(expr match $FQDN '\(\(dev\)\?gdx[0-9]\{3,4\}.orsay.grid5000.fr\)')"  == "$FQDN" ] ;
		then
			echo ""
		else
			echo ERR_PREFIX "orsay_get_cluster, Strange FQDN=$FQDN for orsay site. Aborting" 1>&2
			exit 1
		fi
	}

	function rennes_get_cluster {
		FQDN=$1
		if [ "$(expr match $FQDN '\(parasol\(-dev\)\?[0-9]\{0,3\}.irisa.fr\)')"  == "$FQDN" ] ;
		then
			echo "parasol"
		elif [ "$(expr match $FQDN '\(paraci\(-dev\)\?[0-9]\{0,3\}.irisa.fr\)')"  == "$FQDN" ] ;
		then
			echo "paraci"
		elif [ "$(expr match $FQDN '\(paravent\(-dev\)\?[0-9]\{0,3\}.irisa.fr\)')"  == "$FQDN" ] ;
		then
			echo "paravent"
		elif [ "$(expr match $FQDN '\(tartopom\(-dev\)\?[0-9]\{0,3\}.irisa.fr\)')"  == "$FQDN" ] ;
		then
			echo "tartopom"
		else
			echo ERR_PREFIX "rennes_get_cluster, Strange FQDN=$FQDN for rennes site. Aborting" 1>&2
			exit 1
		fi

	}

	function sophia_get_cluster {
		FQDN=$1
		if [ "$FQDN" == "frontale.sophia.grid5000.fr" ] ;
		then
			echo "azur"
		elif [ "$(expr match $FQDN '\(node-[0-9]\{1,3\}.sophia.grid5000.fr\)')" == "$FQDN" ] ;
		then
			echo "azur"
		elif [ "$FQDN" == "stock.sophia.grid5000.fr" ] ;
		then
			echo "helios"
		elif [ "$(expr match $FQDN '\(helios[0-9]\{1,3\}.sophia.grid5000.fr\)')" == "$FQDN" ] ;
		then
			echo "helios"	
		else
			echo ERR_PREFIX "sophia_get_cluster, Strange FQDN=$FQDN for sophia site. Aborting" 1>&2
			exit 1
		fi
	}

	function toulouse_get_cluster {
		FQDN=$1
		if [ "$(expr match $FQDN '\(citc-[0-9]\{1,3\}.toulouse.grid5000.fr\)')" == "$FQDN" ] ;
		then
			echo ""
		else
			echo ERR_PREFIX "toulouse_get_cluster, Strange FQDN=$FQDN for toulouse site. Aborting" 1>&2
			exit 1
		fi
	}

	# Check that we are on grid5000
	if [ "`expr match "$FQDN" '.*\(grid5000.fr\)'`" == "grid5000.fr" ] ;
	then
		# We are on a grid5000's machine

		# Extract the site from FQDN
		LOCAL=`expr match "$FQDN" '\(.*\).grid5000.fr'`
		SITE=`expr match "$LOCAL" '.*\.\([a-zA-Z]\+\)'`	

		# Extract the tupple <cluster, site> from FQDN
		echo `${SITE}_get_cluster $FQDN`.$SITE


	elif [ "`expr match "$FQDN" '.*\(irisa.fr\)'`"  == "irisa.fr" ] ;
	then
		echo `rennes_get_cluster $FQDN`.rennes

	elif [ "`expr match "$FQDN" '.*\(imag.fr\)'`"  == "imag.fr" ] ;
	then
		echo `grenoble_get_cluster $FQDN`.grenoble
	else
		echo $ERR_PREFIX "get_cluster, not on Grid5000 FQDN=$FQDN" 1>&2
		exit 1
	fi

	return 0
}

function cluster2site {
  case $1 in
    paraci.rennes)   echo rennes ;;
    parasol.rennes)  echo rennes ;;
    paravent.rennes) echo rennes ;;
    tartopom.rennes) echo rennes ;;
    azur.sophia)     echo sophia ;;
    helios.sophia)   echo sophia ;;
    # Note: each of idpot & icluster2 has its own NFS
    *) 
    	# TODO: Check taht $1 is a valid cluster
    	echo $1
  esac
}

function resolv_cluster_alias {
  case $1 in
	helios)   echo helios.sophia     ;;
	azur)     echo azur.sophia       ;;
	paraci)   echo paraci.rennes     ;;
	parasol)  echo parasol.rennes    ;;
	paravent) echo paravent.rennes   ;;
	tartopom) echo tartopom.rennes   ;;
	idpot)    echo idpot.grenoble    ;;
	icluster2)echo icluster2.grenoble;;
	*) 
		# TODO: Check that $1 is a valid cluster
		echo $1
	;;
  esac	
}

function get_oar_hostname {
	CLUSTER=$1
	echo oar.${CLUSTER}.grid5000.fr
}

function get_frontale_hostname {
	CLUSTER=$1
  	echo frontale.${CLUSTER}.grid5000.fr
}

function get_sync_hostname {
       CLUSTER=$1
       SITE=$(cluster2site $CLUSTER)
       echo sync.${SITE}.grid5000.fr
}

function allow_direct_ssh {
	CLUSTER=$1
  	if [ "$CLUSTER" == "azur.sophia" ]   ; then return 0 ;fi
  	if [ "$CLUSTER" == "helios.sophia" ] ; then return 0 ;fi

	return 1
}

LOCAL_CLUSTER=$(get_cluster)
LOCAL_SITE=$(cluster2site $LOCAL_CLUSTER)

