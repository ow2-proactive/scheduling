#! /bin/bash

PATH="$PATH:/sbin"
MACHINES=( "$(hostname)" )

function print_help {
	echo "Usage: $0 [-a|-m machine]"
	echo "           -a, kill ProActive JVM on all machines belonging to m-oasis group"
	echo "           -m, kill ProActive JVM on this machine only"
	echo "               This option ca be present more than one time "	
	exit 0	
}

function killProActive {
	echo "Doing it on $(hostname)"
	
	for PID in $(pidof java); do
			# We do not want to kill eclipse is the workspace is named "ProActive"
	        eclipse=$(grep -c -i eclipse /proc/$PID/cmdline)
	        if [ "$eclipse" = 0 ] ; then
	                grep -i proactive /proc/$PID/cmdline && kill -9 $PID 2>/dev/null
	        fi
	done
	
	killall -9 rmid rmiregistry
}

while getopts "aem:" Option
do
        case $Option in
  			m) 
  				MACHINES=( "${MACHINES[@]}" "$OPTARG" ) 
  				;;
        	a)
        		source /usr/local/bashutil/autoload_lib
				autoload_lib /usr/local/bashutil/lib/batch
        		MACHINES=( $(netgroup_ls m-oasis) )
        		;;
        	e)  
        		killProActive
        		exit 0
        		;;
        	h) print_help ;;
        	*) print_help ;;
		esac
done

# readlink -e is not available on RH6. 
# This hack should work...
EXECFILE=$0
if [ $(expr "$0" : "\." ) = "1" ] ; then
	if [ -x "$PWD/$0" ] ; then
		EXECFILE="$PWD/$0"
	else
		echo "ERROR: Cannot get the absolute path of this script"
		exit 1
	fi
fi

/usr/local/bin/maprsh "$EXECFILE -e" ${MACHINES[@]}
