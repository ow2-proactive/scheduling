#!/bin/bash

OPTIONS_CLUSTERS=("$(get_cluster)")
MODOPT_OPTS="aos:"

SEEN_OPT_A="No"
SEEN_OPT_S="No"
SEEN_OPT_O="No"


function modopt_print_help {
	echo -e "\nTarget:\n"
	echo -e "  By default action are performed on the local cluster only"
	echo -e "\t-s cluster     , execute this command on the specified cluster only"
	echo -e "\t-a             , execute this command on all clusters"
	echo -e "\t-o             , execute this command on all clusters, local one excepted"
}

function modopt_error_handle {
	case $1 in
	a) return 1 ;;
	o) return 1 ;;
	s) return 1 ;;
	esac

	return 0 
}



function modopt_parse {
        while getopts ":${MODOPT_OPTS}" Option
        do
                case $Option in
                a)
			if [ "$SEEN_OPT_S" == "Yes" ] || [ "$SEEN_OPT_O" == "Yes" ] ; then
				echo "Option -a, -o and -s are incompatible. Try -h to get more help" 1>&2
				exit 1
			fi
			SEEN_OPT_A="Yes" 
			
			OPTIONS_CLUSTERS=("${CLUSTERS[@]}")
		;;
                s)
			if [ "$SEEN_OPT_A" == "Yes" ] || [ "$SEEN_OPT_O" == "Yes" ] ; then
				echo "Option -a, -o and -s are incompatible. Try -h to get more help" 1>&2
				exit 1
			fi

			if [ "$OPTARG" == "help" ] ; then
				echo "Available clusters are:" 1>&2
				for i in `seq 0 $((${#CLUSTERS[@]}-1))` ; 
				do
					CLUSTER=${CLUSTERS[$i]}
					echo -e "\t$CLUSTER" 1>&2
				done
				exit 1
			fi


			if [ "$SEEN_OPT_S" == "No" ] ;
			then
				OPTIONS_CLUSTERS=("$(resolv_cluster_alias $OPTARG)")
			else 
				OPTIONS_CLUSTERS=("${OPTIONS_CLUSTERS[@]}" $(resolv_cluster_alias $OPTARG))
			fi
			SEEN_OPT_S="Yes" 
			
		;;
		o)
			if [ "$SEEN_OPT_S" == "Yes" ] || [ "$SEEN_OPT_A" == "Yes" ] ; then
				echo "Option -a, -o and -s are incompatible. Try -h to get more help" 1>&2
				exit 1
			fi
			SEEN_OPT_O="Yes" 
			OPTIONS_CLUSTERS=()
			for i in `seq 0 ${#CLUSTERS[*]}` ; do
				CLUSTER=${CLUSTERS[$i]}
				if [ "$CLUSTER" != "$(get_cluster)" ] ;
				then
					OPTIONS_CLUSTERS=("${OPTIONS_CLUSTERS[@]}" $CLUSTER)
				fi
			done
		;;
                esac
        done

	OPTIND=0

	if [ "$VERBOSE" == "Yes" ] ;
	then
		echo "SEEN_OPT_A=$SEEN_OPT_A" 1>&2
		echo "SEEN_OPT_O=$SEEN_OPT_O" 1>&2
		echo "SEEN_OPT_S=$SEEN_OPT_S" 1>&2
		echo "OPTIONS_CLUSTERS=${OPTIONS_CLUSTERS[@]}" 1>&2
		echo "" 1>&2
	fi
}

