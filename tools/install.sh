#!/bin/bash

if [[ $(id -u) -ne 0 ]] ; then echo "Please run the installation script as root" ; exit 1 ; fi

SCRIPT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"
INSTALL_PADIR="$(dirname "$SCRIPT_DIR")"
PA_FOLDER_NAME="$(basename "$INSTALL_PADIR")"
OLD_PADIR=
OS=
PKG_TOOL=

if which dnf > /dev/null 2>&1; then
   OS="RedHat"
   PKG_TOOL="dnf"
elif which yum > /dev/null 2>&1; then
   OS="RedHat"
   PKG_TOOL="yum"
elif which apt-get > /dev/null 2>&1; then
   OS="Debian"
   PKG_TOOL="apt-get"
else
    echo "This Operating system is not supported by the Proactive installer."; exit 1 ;
fi

RSYNC_VER=$( rsync --version|grep "protocol version"|awk '{print $6}' )

if [[ $RSYNC_VER -ge 31 ]]; then
	RSYNC_PROGRESS="--info=progress2"
else
	RSYNC_PROGRESS="--progress"
fi

confirm() {
    # call with a prompt string or use a default
    read -r -e -p "${1:-Are you sure? [Y/n]} " -i "${2:-y}" yn
    yn=${yn,,}    # tolower
    yn=$(echo $yn | xargs)   # trim
    if [[ $yn =~ ^(yes|y|)$ ]]; then
            true
    else
            false
    fi
}

echo "This will install the ProActive scheduler as a service."
echo "Once the installer is started, it must process until the end otherwise the installation may be corrupted"


if confirm "Do you want to continue? [Y/n] " ; then
    :
else
    exit 1
fi

# checking java installation

JAVA_CMD=

if [ ! -f "$INSTALL_PADIR/jre/bin/java" ]; then
    if [[ "$JAVA_HOME" == "" ]]; then
	    if ! which java > /dev/null 2>&1; then
            echo "JAVA_HOME is not set, no 'java' command could be found in your PATH, and this ProActive distribution does not contain an embedded jre, please install a JRE or JDK." ; exit 1 ;
        else
            JAVA_CMD=java
        fi
    else
        JAVA_CMD="$JAVA_HOME/bin/java"
    fi
fi

if ! which git > /dev/null 2>&1; then
     echo "GIT is not installed on this computer and is required by the ProActive installation."
     if confirm "Do you want to install it? [Y/n] " ; then
         if [[ "$OS" == "RedHat" ]]; then
            $PKG_TOOL -y install git
         elif [[ "$OS" == "Debian" ]]; then
            $PKG_TOOL -y install git
         fi
     fi
fi

# stopping service

if [ -f /etc/init.d/proactive-scheduler ]; then
    service proactive-scheduler stop
fi

# installation of the ProActive distribution
 
read -e -p "Directory where to install the scheduler: " -i "/opt/proactive" PA_ROOT

PA_ROOT=$(echo $PA_ROOT | xargs)

mkdir -p $PA_ROOT

# handle overwriting an existing installation and finding the previous installation
if [ -d "$PA_ROOT/$PA_FOLDER_NAME" ]; then
    if confirm "A folder $PA_ROOT/$PA_FOLDER_NAME already exists. This installer must delete its content and replace it by a fresh install, do you want to continue? [Y/n] " ; then
        if [ -h "$PA_ROOT/previous" ]; then
            OLD_PADIR=$(readlink "$PA_ROOT/previous")
        fi

        rm -rf $PA_ROOT/$PA_FOLDER_NAME
        rm -f $PA_ROOT/default
    else
        exit 1
    fi
else
    # checking the previous installation
    if [ -h "$PA_ROOT/default" ]; then
        OLD_PADIR=$(readlink "$PA_ROOT/default")
        rm -f $PA_ROOT/default
    fi
fi

rsync $RSYNC_PROGRESS -a $INSTALL_PADIR $PA_ROOT

ln -s -f $PA_ROOT/$PA_FOLDER_NAME "$PA_ROOT/default"


# creation of the proactive user

read -e -p "Name of the user starting the ProActive service: " -i "proactive" USER

USER=$(echo $USER | xargs)

read -e -p "Group of the user starting the ProActive service: " -i "proactive" GROUP

GROUP=$(echo $GROUP | xargs)

id -u "$USER" > /dev/null

if (( $? != 0 )); then
    echo "The user $USER does not exist. The installation script must create this user."
    if confirm "Proceed ? [Y/n] "; then
        if [ "$GROUP" == "$USER" ]; then
            useradd $USER -d $PA_ROOT
        else
            useradd $USER -d $PA_ROOT -g $GROUP
        fi
        passwd $USER;
    else
        exit 1
    fi

fi

chown $USER:$GROUP $PA_ROOT
chown $USER:$GROUP $PA_ROOT/default
chown -R $USER:$GROUP $PA_ROOT/$PA_FOLDER_NAME


# Configuration of the service script

if [ -f /etc/init.d/proactive-scheduler ] && [[ "$OLD_PADIR" != "" ]]; then
    # backup previous service file
    cp /etc/init.d/proactive-scheduler $OLD_PADIR/config/
fi

cp $SCRIPT_DIR/proactive-scheduler /etc/init.d/

read -e -p "Protocol used by the proactive server: [http/https] " -i "http" PROTOCOL

PROTOCOL=$(echo $PROTOCOL | xargs)

read -e -p "Port used by the proactive server: " -i "8080" PORT

PORT=$(echo $PORT | xargs)

read -e -p "Number of ProActive nodes to start on the server machine: " -i "4" NB_NODES
NB_NODES=$(echo $NB_NODES | xargs)

if confirm "Setup cron task for cleaning old logs? [Y/n] " ; then
     read -e -p "Cleaning logs older than (in days) [50]: " -i "50" LOGS_CLEANUP_DAYS

     LOGS_CLEANUP_DAYS=$(echo $LOGS_CLEANUP_DAYS | xargs)
     if [[ $(grep -c "$PA_ROOT/default/logs" /etc/crontab) == 0 ]]; then
        echo "1 0   * * *   root   find $PA_ROOT/default/logs -mtime +$LOGS_CLEANUP_DAYS -name '*.log' -exec rm {} \;" >> /etc/crontab
     fi
fi



# Escape functions for sed 
escape_rhs_sed ()
{
        echo $(printf '%s\n' "$1" | sed 's:[\/&]:\\&:g;$!s/$/\\/')

}

sed -e "s/^USER=.*/USER=$USER/g"  -i "/etc/init.d/proactive-scheduler"
sed -e "s/^PROTOCOL=.*/PROTOCOL=$PROTOCOL/g"  -i "/etc/init.d/proactive-scheduler"
if  [[ "$PROTOCOL" == "https" ]]; then
    sed -e "s/^web\.https=.*/web.https=true/g"  -i "$PA_ROOT/default/config/web/settings.ini"
fi
sed -e "s/^PORT=.*/PORT=$PORT/g"  -i "/etc/init.d/proactive-scheduler"
sed -e "s/^web\.port=.*/web.port=$PORT/g"  -i "$PA_ROOT/default/config/web/settings.ini"
sed -e "s/^NB_NODES=.*/NB_NODES=$NB_NODES/g"  -i "/etc/init.d/proactive-scheduler"
sed -e "s/^PA_ROOT=.*/PA_ROOT=$(escape_rhs_sed "$PA_ROOT")/g"  -i "/etc/init.d/proactive-scheduler"

if confirm "Start ProActive Nodes in a single JVM process (y) or multiple JVM Processes (n) ? [Y/n] " ; then
    sed -e "s/^SINGLE_JVM=.*/SINGLE_JVM=true/g"  -i "/etc/init.d/proactive-scheduler"
fi

echo "Here are the network interfaces available on your machine and the interface which will be automatically selected by ProActive: "

if [[ "$JAVA_CMD" == "" ]]; then
   JAVA_CMD=$PA_ROOT/default/jre/bin/java
fi

# Select network interface

NETWORK_OUTPUT=$( $JAVA_CMD -cp "$PA_ROOT/default/dist/lib:$PA_ROOT/default/dist/lib/*" org.objectweb.proactive.core.util.ProActiveInet )
echo "$NETWORK_OUTPUT"


if confirm "Do you want to change the network interface used? [y/N] " "n" ; then
     ITF_ARRAY=( $( echo "$NETWORK_OUTPUT" | grep -e 'MAC:' | awk '{ print $1 }' ) )
     echo "Available interfaces : " "${ITF_ARRAY[@]}"
     ITF_SELECTED=false
     while  ! $ITF_SELECTED ; do
        read -e -p "Enter the interface name you want to use: " ITF_NAME
        ITF_NAME=$(echo $ITF_NAME | xargs)

        if [[ " ${ITF_ARRAY[@]} " =~ " ${ITF_NAME} " ]]; then
            ITF_SELECTED=true
        else
            echo "Interface $ITF_NAME not in the list"
        fi
     done

     echo "proactive.net.interface=$ITF_NAME" >> "$PA_ROOT/default/config/network/server.ini"
     echo "proactive.net.interface=$ITF_NAME" >> "$PA_ROOT/default/config/network/node.ini"
fi


# installation of the proactive-scheduler service

chmod 700 /etc/init.d/proactive-scheduler

mkdir -p /var/log/proactive
touch /var/log/proactive/scheduler

chown -R $USER:$GROUP /var/log/proactive

if confirm "Start the proactive-scheduler service at startup? [Y/n] " ; then
    if [[ "$OS" == "RedHat" ]]; then
       chkconfig proactive-scheduler on
    elif [[ "$OS" == "Debian" ]]; then
       update-rc.d proactive-scheduler defaults
    fi
fi

CONFLICT=false

if which git > /dev/null 2>&1; then
    # Porting of older version configuration and data files

    OLD_PWD=$(pwd)

    cd $PA_ROOT/default/config

    # version as well the proactive-scheduler service file
    cp /etc/init.d/proactive-scheduler .

    git init
    git config user.email "support@activeeon.com"
    git config user.name "proactive"
    git add -A .
    git commit -m "Initial Commit for $PA_FOLDER_NAME"
    git tag -a "root" -m "Root commit"


    if [[ "$OLD_PADIR" != "" ]]; then
        echo ""
        echo "Detected an existing ProActive Scheduler installation at $OLD_PADIR, porting configuration into new installation."
        echo ""

        rsync $RSYNC_PROGRESS -a $OLD_PADIR/addons $PA_ROOT/default/

        OLD_PADIR_NAME="$(basename "$OLD_PADIR")"

        cd $OLD_PADIR/config

        # Commit uncommitted changes in the old repository
        git add -A .
        git commit -m "Commit all changes before switching from $OLD_PADIR_NAME to $PA_FOLDER_NAME"
        NB_COMMITS=$(git rev-list --count HEAD)
        NB_COMMITS=$( expr $NB_COMMITS - 1 )

         cd $PA_ROOT/default/config

        # Apply all commits from the previous repository to the new one
        git remote add old-version $OLD_PADIR/config/.git
        git fetch old-version

        if [[ NB_COMMITS -gt 0 ]]; then
            ROOT_ID=$( git rev-parse old-version/master~$NB_COMMITS)
            MASTER_ID=$( git rev-parse old-version/master )
            echo ""
            echo "Cherry-picking all changes to new installation"
            echo ""
            git cherry-pick -x ${ROOT_ID}..$MASTER_ID

            if (( $? != 0 )); then
                echo ""
                echo "A conflict occurred, cd to $PA_ROOT/default/config and follow the instructions displayed by git to resolve them."
                echo "Additionnaly, if a conflict occurs on the file $PA_ROOT/default/config/proactive-scheduler,"
                echo "you will need to manually copy the modified file after conflicts are resolved by using the command:"
                echo "cp $PA_ROOT/default/config/proactive-scheduler /etc/init.d/"
                CONFLICT=true
            fi
        fi

        # copy merged changes on the service (if ever a conflict occurs, the user will have to manually copy the merge)
        cp proactive-scheduler /etc/init.d/


        if ls $PA_ROOT/default/addons/*.jar > /dev/null 2>&1; then
            # display the list of addons in the new installation
            echo ""
            echo "Here is the list of jar files in the new installation 'addons' folder."
            echo "If there are duplicates, you need to manually remove outdated versions."
            echo ""

            ls -l $PA_ROOT/default/addons/*.jar
        fi

    fi
    cd $OLD_PWD
else
    # in case we don't use git
    rsync $RSYNC_PROGRESS -a $OLD_PADIR/{addons,data,config} $PA_ROOT/default/
fi

if [[ "$OLD_PADIR" != "" ]]; then
    ln -s -f $OLD_PADIR "$PA_ROOT/previous"
fi

chown -R $USER:$GROUP $PA_ROOT/$PA_FOLDER_NAME

if confirm "Restrict the access to the ProActive installation folder to user $USER ? [Y/n] " ; then
    # preserve credentials access
    chmod -R go-rwx $PA_ROOT/$PA_FOLDER_NAME
fi

if  ! $CONFLICT ; then
    if confirm "Do you want to start the scheduler service now? [Y/n] " ; then
        service proactive-scheduler start
    fi
fi



