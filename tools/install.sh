#!/bin/bash

SCRIPT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"

# installation of the ProActive distribution
 
read -e -p "Directory where to install the scheduler: " -i "/opt/proactive" PA_ROOT

mkdir -p $PA_ROOT
rm -f $PA_ROOT/default

CURRENT_PADIR="$(dirname "$SCRIPT_DIR")"
PA_FOLDER_NAME="$(basename "$CURRENT_PADIR")"

/usr/bin/cp -f -R $CURRENT_PADIR $PA_ROOT

ln -s -f $PA_ROOT/$PA_FOLDER_NAME $PA_ROOT/default

# creation of the proactive user

read -e -p "Name of the user starting the ProActive service: " -i "proactive" USER

echo "Do you want to create this user ?"
select yn in "Yes" "No"; do
    case $yn in
        Yes ) 
		useradd -m $USER;
		passwd $USER; 
		break;;
        No ) break;;
    esac
done

chown -R $USER:$USER $PA_ROOT/$CURRENT_PA_DIR

# installation of the service script

cp $SCRIPT_DIR/proactive-scheduler /etc/init.d/

read -e -p "Protocol used by the proactive server: " -i "http" PROTOCOL

read -e -p "Port used by the proactive server: " -i "8080" PORT

read -e -p "Number of ProActive nodes to start on the server: " -i "4" NB_NODES


# Escape functions for sed 
escape_rhs_sed ()
{
        echo $(printf '%s\n' "$1" | sed 's:[\/&]:\\&:g;$!s/$/\\/')

}

sed "s/USER=.*/USER=$USER/g"  -i "/etc/init.d/proactive-scheduler"
sed "s/PROTOCOL=.*/PROTOCOL=$PROTOCOL/g"  -i "/etc/init.d/proactive-scheduler"
sed "s/PORT=.*/PORT=$PORT/g"  -i "/etc/init.d/proactive-scheduler"
sed "s/NB_NODES=.*/NB_NODES=$NB_NODES/g"  -i "/etc/init.d/proactive-scheduler"
sed "s/PA_ROOT=.*/PA_ROOT=$(escape_rhs_sed "$PA_ROOT")/g"  -i "/etc/init.d/proactive-scheduler"


chmod 700 /etc/init.d/proactive-scheduler

mkdir -p /var/log/proactive
touch /var/log/proactive/scheduler

chown -R $USER:$USER /var/log/proactive

echo "Start the proactive-scheduler service at startup ?"
select yn in "Yes" "No"; do
    case $yn in
        Yes )
		chkconfig proactive-scheduler on;
                break;;
        No ) 	break;;
    esac
done




