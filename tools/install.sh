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
    echo "This operating system is not supported by the ProActive installer."; exit 1 ;
fi

RSYNC_VER=$( rsync --version|grep "protocol version"|awk '{print $6}' )

if [[ $RSYNC_VER -ge 31 ]]; then
    RSYNC_PROGRESS="--info=progress2"
else
    RSYNC_PROGRESS="--progress"
fi

trim() {
    echo "$1" | sed -e 's/^[[:space:]]*//'|sed -e 's/[[:space:]]*$//'
}

confirm() {
    # call with a prompt string or use a default
    read -r -e -p "${1:-Are you sure? [Y/n]} " -i "${2:-y}" yn
    yn=${yn,,}    # tolower
    yn=$(trim "$yn")
    if [[ $yn =~ ^(yes|y|)$ ]]; then
            true
    else
            false
    fi
}

# Escape functions for sed
escape_rhs_sed ()
{
        echo $(printf '%s\n' "$1" | sed 's:[\/&]:\\&:g;$!s/$/\\/')

}

escape_lhs_sed ()
{
        echo $(printf '%s\n' "$1" | sed 's:[][\/.^$*]:\\&:g')

}

compute-version ()
{
    echo $(ls $1/dist/lib/scheduler-server-*.jar | sed "s/^$(escape_lhs_sed $1/dist/lib/scheduler-server-)\(.*\)\.jar$/\1/g")

}

update_node_jars()
{
    ( cd $PA_ROOT/default && zip -f dist/lib/rm-node-*.jar config/authentication/rm.cred )
    ( cd $PA_ROOT/default/dist && zip -f war/rest/node.jar lib/rm-node-*.jar )
}

reuse_accounts()
{
    echo "Retrieving private/public keys from previous scheduler installation"
    /bin/cp $OLD_PADIR/config/authentication/keys/*.key $AUTH_ROOT/keys/

    echo "Retrieving accounts from previous scheduler installation"
    /bin/cp $OLD_PADIR/config/authentication/login.cfg $OLD_PADIR/config/authentication/group.cfg $AUTH_ROOT/

    echo "Retrieving credential files from previous scheduler installation"
    /bin/cp $OLD_PADIR/config/authentication/*.cred $AUTH_ROOT/

    echo "Retrieving keystore files from previous scheduler installation"
    /bin/cp $OLD_PADIR/config/web/keystore $PA_ROOT/default/config/web/
    if [ -f $OLD_PADIR/config/web/truststore ]; then
        /bin/cp $OLD_PADIR/config/web/truststore $PA_ROOT/default/config/web/
    fi

    update_node_jars
}

ask_admin_password()
{
    ADMIN_PWD_ENTERED=false
    while  ! $ADMIN_PWD_ENTERED ; do
        read -s -p "Enter ProActive \"admin\" account password: " ADMIN_PWD
        echo ""
        read -s -p "Retype \"admin\" account password: " ADMIN_PWD2
        echo ""
        if [[ "$ADMIN_PWD" == "$ADMIN_PWD2" ]]; then
                ADMIN_PWD_ENTERED=true
        else
                ADMIN_PWD_ENTERED=false
                echo "Passwords don't match."

        fi
    done

}

generate_new_accounts()
{
    # generate random password for internal scheduler accounts
    echo "Generating random password for internal scheduler accounts."
    RM_PWD=$(date +%s | sha256sum | base64 | head -c 32 ; echo)
    SCHED_PWD=$(date +%s | sha256sum | base64 | head -c 32 ; echo)
    WATCHER_PWD=$(date +%s | sha256sum | base64 | head -c 32 ; echo)

    ask_admin_password

    # generate new private/public key pair


    echo "Generating New Private/Public key pair for the scheduler"
    $PA_ROOT/default/tools/proactive-key-gen -p "$AUTH_ROOT/keys/priv.key" -P "$AUTH_ROOT/keys/pub.key"


    $PA_ROOT/default/tools/proactive-users -U -l admin -p "$ADMIN_PWD"
    $PA_ROOT/default/tools/proactive-users -U -l rm -p "$RM_PWD"
    $PA_ROOT/default/tools/proactive-users -U -l scheduler -p "$SCHED_PWD"
    $PA_ROOT/default/tools/proactive-users -U -l watcher -p "$WATCHER_PWD"

    echo "Generating credential files for ProActive System accounts"

    $PA_ROOT/default/tools/proactive-create-cred  -F $AUTH_ROOT/keys/pub.key -l admin -p "$ADMIN_PWD" -o $AUTH_ROOT/admin_user.cred
    $PA_ROOT/default/tools/proactive-create-cred  -F $AUTH_ROOT/keys/pub.key -l scheduler -p "$SCHED_PWD" -o $AUTH_ROOT/scheduler.cred
    $PA_ROOT/default/tools/proactive-create-cred  -F $AUTH_ROOT/keys/pub.key -l rm -p "$RM_PWD" -o $AUTH_ROOT/rm.cred
    $PA_ROOT/default/tools/proactive-create-cred  -F $AUTH_ROOT/keys/pub.key -l watcher -p "$WATCHER_PWD" -o $AUTH_ROOT/watcher.cred

    update_node_jars

    # configure watcher account
    sed "s/scheduler\.cache\.password=.*/scheduler.cache.password=/g"  -i "$PA_ROOT/default/config/web/settings.ini"
    sed "s/^.*scheduler\.cache\.credential=.*$/scheduler.cache.credential=$(escape_rhs_sed $AUTH_ROOT/watcher.cred)/g"  -i "$PA_ROOT/default/config/web/settings.ini"
    sed "s/rm\.cache\.password=.*/rm.cache.password=/g"  -i "$PA_ROOT/default/config/web/settings.ini"
    sed "s/^.*rm\.cache\.credential=.*$/rm.cache.credential=$(escape_rhs_sed $AUTH_ROOT/watcher.cred)/g"  -i "$PA_ROOT/default/config/web/settings.ini"

    # removing test accounts

    $PA_ROOT/default/tools/proactive-users -D -l demo
    $PA_ROOT/default/tools/proactive-users -D -l user
    $PA_ROOT/default/tools/proactive-users -D -l guest
    $PA_ROOT/default/tools/proactive-users -D -l test
    $PA_ROOT/default/tools/proactive-users -D -l radmin
    $PA_ROOT/default/tools/proactive-users -D -l nsadmin
    $PA_ROOT/default/tools/proactive-users -D -l provider
    $PA_ROOT/default/tools/proactive-users -D -l test_executor

}

adding_files()
{
    OLD_PWD=$(pwd)
    cd "$PA_DIR"
    git add -A config

    git add bin/proactive-server
    git add tools/proactive-scheduler
    git add tools/*.groovy

    git add dist/war/rm/rm.conf
    git add dist/war/scheduler/scheduler.conf

    for file in $( find dist/war -name 'application.properties' ); do
        git add "$file"
    done
    cd "$OLD_PWD"
}

staging_changes()
{
    OLD_PWD=$(pwd)
    cd "$OLD_PADIR"
    git add -u .
    cd "$OLD_PWD"
}

init_and_ignores()
{
    OLD_PWD=$(pwd)
    cd "$PA_DIR"
    git init
    git config user.email "support@activeeon.com"
    git config user.name "proactive"
    echo '
config/authentication/*.cred
config/authentication/login.cfg
config/authentication/group.cfg
config/authentication/keys/*.key
config/web/keystore
config/web/truststore
    ' > .gitignore
    cd "$OLD_PWD"
}

initial_commit()
{
    OLD_PWD=$(pwd)
    cd "$PA_DIR"
    git commit -m "Initial configuration files for $PA_FOLDER_NAME"
    git tag -f -a "root" -m "Root commit"
    cd "$OLD_PWD"
}

NEW_VERSION=$(compute-version $INSTALL_PADIR)

echo "This will install the ProActive scheduler $NEW_VERSION as a service."
echo "Once the installer is started, it must process until the end otherwise the installation may be corrupted."


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
            echo "JAVA_HOME is not set, no 'java' command could be found in your PATH, and this ProActive distribution does not contain an embedded JRE, please install a JRE or JDK." ; exit 1 ;
        else
            JAVA_CMD=java
        fi
    else
        JAVA_CMD="$JAVA_HOME/bin/java"
    fi
fi

if ! which git > /dev/null 2>&1; then
     echo "Git is not installed on this computer and is required by the ProActive installation."
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

PA_ROOT=$(trim "$PA_ROOT")

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
        OLD_PADIR=$(readlink -f "$PA_ROOT/default")
        rm -f $PA_ROOT/previous
        ln -s -f $OLD_PADIR "$PA_ROOT/previous"
        rm -f $PA_ROOT/default
    elif [ -h "$PA_ROOT/previous" ]; then
        OLD_PADIR=$(readlink -f "$PA_ROOT/previous")
    fi
fi

rsync $RSYNC_PROGRESS -a $INSTALL_PADIR $PA_ROOT

ln -s -f $PA_ROOT/$PA_FOLDER_NAME "$PA_ROOT/default"

AUTH_ROOT=$PA_ROOT/default/config/authentication

PA_DIR="$(readlink -f "$PA_ROOT/default")"

if [[ "$OLD_PADIR" == "" ]]; then

    # creation of the proactive user

    read -e -p "Name of the user starting the ProActive service: " -i "proactive" USER

    USER=$(trim "$USER")

    read -e -p "Group of the user starting the ProActive service: " -i "proactive" GROUP

    GROUP=$(trim "$GROUP")

    id -u "$USER" > /dev/null

    if (( $? != 0 )); then
        echo "The user $USER does not exist. The installation script must create this user."
        if confirm "Proceed? [Y/n] "; then
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

else
    USER="$(stat -c "%U" "$OLD_PADIR")"
    GROUP="$(stat -c "%G" "$OLD_PADIR")"
    echo "Previous installation was owned by user $USER with group $GROUP"
fi

init_and_ignores
adding_files
initial_commit

chown $USER:$GROUP $PA_ROOT
chown $USER:$GROUP $PA_ROOT/default
chown -R $USER:$GROUP $PA_ROOT/$PA_FOLDER_NAME

echo "ProActive use internal system accounts which should be modified in a production environment."

if [[ "$OLD_PADIR" != "" ]]; then
{
    echo "Reusing accounts from the previous version $OLD_PADIR"
    reuse_accounts
}
elif confirm "Do you want to regenerate the internal accounts ? [Y/n]" ; then
    generate_new_accounts
fi

if [ -f /etc/init.d/proactive-scheduler ] && [[ "$OLD_PADIR" != "" ]]; then
    # backup previous service file
    /bin/cp /etc/init.d/proactive-scheduler "$OLD_PADIR/tools/"
fi

if [[ "$OLD_PADIR" == "" ]]; then
    echo "ProActive can integrate with Linux PAM (Pluggable Authentication Modules) to authenticate users of the linux system."
    echo "Warning: this will add the $USER account to the linux \"shadow\" group"

    if confirm "Do you want to set PAM integration for ProActive scheduler? [y/N] " "n" ; then
         sed "s/pa\.rm\.authentication\.loginMethod=.*/pa.rm.authentication.loginMethod=RMPAMLoginMethod/g"  -i "$PA_ROOT/default/config/rm/settings.ini"
         sed "s/pa\.scheduler\.core\.authentication\.loginMethod=.*/pa.scheduler.core.authentication.loginMethod=SchedulerPAMLoginMethod/g"  -i "$PA_ROOT/default/config/scheduler/settings.ini"
         cp $AUTH_ROOT/proactive-jpam /etc/pam.d/
         usermod -a -G shadow $USER
         echo "Users wil be able to authenticate to the ProActive server with their linux account credentials."
         echo "Groups must still be configured for each user in the $AUTH_ROOT/group.cfg file."
    fi

    # Configuration of the service script

    read -e -p "Protocol to use for accessing Web apps deployed by the proactive server: [http/https] " -i "http" PROTOCOL

    PROTOCOL=$(trim "$PROTOCOL")

    SELF_SIGNED=false
    if [[ "$PROTOCOL" == "https" ]]; then
        if confirm "Do you want to use the provided self-signed certificate? [Y/n] " ; then
            SELF_SIGNED=true
        else
            echo "In order to install a signed certificate, you need to follow the manual configuration steps described in the ProActive documentation :"
            echo "http://doc.activeeon.com/latest/admin/ProActiveAdminGuide.html#_enable_https"
        fi

    fi

    if [[ "$PROTOCOL" == "https" ]]; then
        DEFAULT_PORT=8443
    else
        DEFAULT_PORT=8080
    fi

    read -e -p "Port to use for accessing Web apps deployed by the proactive server: " -i "$DEFAULT_PORT" PORT

    PORT=$(trim "$PORT")

    HTTP_REDIRECT_PORT=8080
    if [[ "$PROTOCOL" == "https" ]]; then
        HTTP_REDIRECT=false
        if confirm "Do you want to redirect an http port to https? [Y/n] " ; then
            HTTP_REDIRECT=true
            read -e -p "Which port do you want to redirect? [8080] " -i "8080" HTTP_REDIRECT_PORT
        fi
    fi

    if [[ "$JAVA_CMD" == "" ]]; then
       JAVA_CMD=$PA_ROOT/default/jre/bin/java
       JAVA_HOME=$PA_ROOT/default/jre
    fi

    if [[ "$PORT" -lt "1024" ]] || [[ "$HTTP_REDIRECT_PORT" -lt "1024" ]] ; then

        if ! which setcap > /dev/null 2>&1; then
            echo "libcap2 is not installed on this computer and is required to start the server on a port below 1024."
            if confirm "Do you want to install it? [Y/n] " ; then
                if [[ "$OS" == "RedHat" ]]; then
                    $PKG_TOOL -y install libcap2
                elif [[ "$OS" == "Debian" ]]; then
                    $PKG_TOOL -y install libcap2-bin
                fi
            fi
        fi
        echo "Enabling priviledge to run server on port lower than 1024"

        if [[ "$JAVA_HOME" == "" ]]; then
            echo "Unable to locate JAVA_HOME, installation will exit."
            exit 1
        fi
        echo "$JAVA_HOME/lib/amd64/jli" > /etc/ld.so.conf.d/proactive-java.conf
        ldconfig | grep libjli
    fi


    read -e -p "Number of ProActive nodes to start on the server machine: " -i "4" NB_NODES
    NB_NODES=$(trim "$NB_NODES")

    echo "The ProActive server can automatically remove from its database old jobs. This feature also remove the associated job logs from the file system."

    if confirm "Do you want to enable automatic job/node history removal? [Y/n] " ; then
         read -e -p "Remove history older than (in days) [30]: " -i "30" JOB_CLEANUP_DAYS
         JOB_CLEANUP_DAYS=$(trim "$JOB_CLEANUP_DAYS")
         JOB_CLEANUP_SECONDS=$((JOB_CLEANUP_DAYS*24*3600))

         # Configure the scheduler to remove jobs
         sed "s/pa\.scheduler\.core\.automaticremovejobdelay=.*/pa.scheduler.core.automaticremovejobdelay=$JOB_CLEANUP_SECONDS/g"  -i "$PA_ROOT/default/config/scheduler/settings.ini"
         sed "s/pa\.scheduler\.job\.removeFromDataBase=.*/pa.scheduler.job.removeFromDataBase=true/g"  -i "$PA_ROOT/default/config/scheduler/settings.ini"
         sed "s/^#pa\.rm\.history\.maxperiod=.*/pa.rm.history.maxperiod=$JOB_CLEANUP_SECONDS/g"  -i "$PA_ROOT/default/config/rm/settings.ini"

         # Cleanup extra log files older than the given period
         if [[ $(grep -c "$PA_ROOT/default/logs" /etc/crontab) == 0 ]]; then
            echo "1 0   * * *   root   find $PA_ROOT/default/logs -mtime +$JOB_CLEANUP_DAYS -name '*.log' -exec rm {} \;" >> /etc/crontab
         fi
    fi



    sed -e "s/^USER=.*/USER=$USER/g" -i "$PA_ROOT/default/tools/proactive-scheduler"
    sed -e "s/^PROTOCOL=.*/PROTOCOL=$PROTOCOL/g" -i "$PA_ROOT/default/tools/proactive-scheduler"

    if [[ "$PROTOCOL" == "https" ]]; then
        sed -e "s/^web\.https=.*/web.https=true/g" -i "$PA_ROOT/default/config/web/settings.ini"
        if $SELF_SIGNED; then
            sed -e "s/^#web\.https\.keystore=\(.*\)/web.https.keystore=\1/g" -i "$PA_ROOT/default/config/web/settings.ini"
            sed -e "s/^#web\.https\.keystore\.password=\(.*\)/web.https.keystore.password=\1/g" -i "$PA_ROOT/default/config/web/settings.ini"
            sed -e "s/^#web\.https\.allow_any_hostname=.*/web.https.allow_any_hostname=true/g" -i "$PA_ROOT/default/config/web/settings.ini"
            sed -e "s/^#web\.https\.allow_any_certificate=.*/web.https.allow_any_certificate=true/g" -i "$PA_ROOT/default/config/web/settings.ini"
            sed -e "s/^#web\.https\.allow_any_hostname=.*/web.https.allow_any_hostname=true/g" -i "$PA_ROOT/default/dist/war/rm/rm.conf"
            sed -e "s/^#web\.https\.allow_any_certificate=.*/web.https.allow_any_certificate=true/g" -i "$PA_ROOT/default/dist/war/rm/rm.conf"
            sed -e "s/^#web\.https\.allow_any_hostname=.*/web.https.allow_any_hostname=true/g" -i "$PA_ROOT/default/dist/war/scheduler/scheduler.conf"
            sed -e "s/^#web\.https\.allow_any_certificate=.*/web.https.allow_any_certificate=true/g" -i "$PA_ROOT/default/dist/war/scheduler/scheduler.conf"
        fi
    fi

    sed -e "s/^PORT=.*/PORT=$PORT/g" -i "$PA_ROOT/default/tools/proactive-scheduler"
    if [[ "$PROTOCOL" == "https" ]]; then
        sed -e "s/^web\.https\.port=.*/web.https.port=$PORT/g" -i "$PA_ROOT/default/config/web/settings.ini"
        if $HTTP_REDIRECT; then
            sed -e "s/^web\.redirect_http_to_https=.*/web.redirect_http_to_https=true/g" -i "$PA_ROOT/default/config/web/settings.ini"
            sed -e "s/^web\.http\.port=.*/web.http.port=$HTTP_REDIRECT_PORT/g" -i "$PA_ROOT/default/config/web/settings.ini"
        fi
    else
        sed -e "s/^web\.http\.port=.*/web.http.port=$PORT/g" -i "$PA_ROOT/default/config/web/settings.ini"
    fi

    for file in $( find $PA_ROOT/default/dist/war -name 'application.properties' ); do
        sed -e "s/$(escape_lhs_sed http://localhost:8080)/$(escape_rhs_sed ${PROTOCOL}://localhost:${PORT})/g" -i "$file"
        if $SELF_SIGNED; then
            sed -e "s/web\.https\.allow_any_certificate=.*/web.https.allow_any_certificate=true/g" -i "$file"
        fi
    done

    sed -e "s/$(escape_lhs_sed http://localhost:8080)/$(escape_rhs_sed ${PROTOCOL}://localhost:${PORT})/g" -i "$PA_ROOT/default/config/web/settings.ini"

    sed -e "s/$(escape_lhs_sed http://localhost:8080)/$(escape_rhs_sed ${PROTOCOL}://localhost:${PORT})/g" -i "$PA_ROOT/default/dist/war/rm/rm.conf"
    sed -e "s/$(escape_lhs_sed http://localhost:8080)/$(escape_rhs_sed ${PROTOCOL}://localhost:${PORT})/g" -i "$PA_ROOT/default/dist/war/scheduler/scheduler.conf"

    sed -e "s/^NB_NODES=.*/NB_NODES=$NB_NODES/g"  -i "$PA_ROOT/default/tools/proactive-scheduler"
    sed -e "s/^PA_ROOT=.*/PA_ROOT=$(escape_rhs_sed "$PA_ROOT")/g" -i "$PA_ROOT/default/tools/proactive-scheduler"

    if [ "$NB_NODES" != "0" ]; then
        sed -e "s/^SINGLE_JVM=.*/SINGLE_JVM=true/g" -i "$PA_ROOT/default/tools/proactive-scheduler"
    fi

    echo "Here are the network interfaces available on your machine and the interface which will be automatically selected by ProActive: "



    # Select network interface

    NETWORK_OUTPUT=$( $JAVA_CMD -cp "$PA_ROOT/default/dist/lib:$PA_ROOT/default/dist/lib/*" org.objectweb.proactive.core.util.ProActiveInet )
    echo "$NETWORK_OUTPUT"


    if confirm "Do you want to change the network interface used by the ProActive server? [y/N] " "n" ; then
         ITF_ARRAY=( $( echo "$NETWORK_OUTPUT" | grep -e 'MAC:' | awk '{ print $1 }' ) )
         echo "Available interfaces : " "${ITF_ARRAY[@]}"
         ITF_SELECTED=false
         while  ! $ITF_SELECTED ; do
            read -e -p "Enter the interface name you want to use: " ITF_NAME
            ITF_NAME=$(trim "$ITF_NAME")

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

    cp "$PA_ROOT/default/tools/proactive-scheduler" /etc/init.d/

    chmod 700 /etc/init.d/proactive-scheduler

    mkdir -p /var/log/proactive
    touch /var/log/proactive/scheduler

    # configure infinite timeout and service type for systemd
    mkdir -p /etc/systemd/system/proactive-scheduler.service.d
    echo '
[Service]
Type=simple
TimeoutSec=0
' > /etc/systemd/system/proactive-scheduler.service.d/timeout.conf



    chown -R $USER:$GROUP /var/log/proactive

    if confirm "Start the proactive-scheduler service at machine startup? [Y/n] " ; then
        if [[ "$OS" == "RedHat" ]]; then
           chkconfig proactive-scheduler on
        elif [[ "$OS" == "Debian" ]]; then
           update-rc.d proactive-scheduler defaults
        fi
    fi
fi

CONFLICT=false

if which git > /dev/null 2>&1; then
    # Porting of older version configuration and data files

    OLD_PWD=$(pwd)

    cd $PA_ROOT/default/

    if [[ "$OLD_PADIR" != "" ]]; then
        echo ""
        echo "Detected an existing ProActive Scheduler installation at $OLD_PADIR"

        echo  "Copying addons and data files from the previous installation..."
        echo ""

        rsync $RSYNC_PROGRESS -a $OLD_PADIR/{addons,data} $PA_ROOT/default/

        if confirm "Do you want to port all configuration changes to the new version (do not do this from a version prior to 8.4.0 before executing the patch)? [Y/n] " ; then
            echo  "Porting configuration into new installation..."
            echo ""

            OLD_PADIR_NAME="$(basename "$OLD_PADIR")"

            cd $OLD_PADIR

            # Commit uncommitted changes in the old repository
            staging_changes

            git commit -m "Commit all changes before switching from $OLD_PADIR_NAME to $PA_FOLDER_NAME"
            NB_COMMITS=$(git rev-list --count HEAD)
            NB_COMMITS=$( expr $NB_COMMITS - 1 )

            cd $PA_ROOT/default

            # Apply all commits from the previous repository to the new one
            git remote add old-version $OLD_PADIR/.git
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
                    echo "A conflict occurred, cd to $PA_ROOT/default/ and follow the instructions displayed by Git to resolve the issues."
                    echo "Additionally, if a conflict occurred on the file $PA_ROOT/default/tools/proactive-scheduler,"
                    echo "you will need to manually copy the modified file after conflicts have been resolved by using the command:"
                    echo "cp $PA_ROOT/default/tools/proactive-scheduler /etc/init.d/"
                    CONFLICT=true
                    read -n 1 -s -r -p "Press any key to continue"
                fi
            fi

            # copy merged changes on the service (if ever a conflict occurs, the user will have to manually copy the merge)
            cp "$PA_ROOT/default/tools/proactive-scheduler" /etc/init.d/
        fi
    fi
    cd $OLD_PWD
else
    # in case we don't use git
    rsync $RSYNC_PROGRESS -a $OLD_PADIR/{addons,data,config} $PA_ROOT/default/
fi

if ls $PA_ROOT/default/addons/*.jar > /dev/null 2>&1; then


    if [[ "$OLD_PADIR" != "" ]]; then
        OLD_VERSION=$(compute-version $OLD_PADIR)
        if [[ "$OLD_VERSION" != "$NEW_VERSION" ]]; then
           rm -f  $PA_ROOT/default/addons/*$OLD_VERSION*.jar
        fi
    fi
    # display the list of addons in the new installation
    echo ""
    echo "Here is the list of JAR files in the new installation 'addons' folder."
    echo "If there are duplicates, you need to manually remove outdated versions."
    echo ""

    ls -l $PA_ROOT/default/addons/*.jar
fi

chown -R $USER:$GROUP $PA_ROOT/$PA_FOLDER_NAME

if confirm "Restrict the access to the ProActive installation folder to user ${USER}? [y/N] " "n" ; then
    # preserve all access
    chmod -R go-rwx $PA_ROOT/$PA_FOLDER_NAME
fi

if confirm "Restrict the access to the configuration files containing sensitive information to user ${USER}? [Y/n] " ; then
    # preserve credentials access
    chmod -R go-rwx $PA_ROOT/$PA_FOLDER_NAME/config
    chmod go-rwx $PA_ROOT/$PA_FOLDER_NAME/dist/war/scheduler/scheduler.conf
    chmod go-rwx $PA_ROOT/$PA_FOLDER_NAME/dist/war/rm/rm.conf
    for file in $( find $PA_ROOT/$PA_FOLDER_NAME/dist/war -name 'application.properties' ); do
        chmod go-rwx $file
    done

fi

echo "Resource Manager credentials are used by remote ProActive Agents to register to the scheduler."
echo "If you plan to use ProActive Agents, please replace in their respective \"schedworker\" folder the following files taken from the server installation:"
ls $PA_ROOT/default/config/authentication/rm.cred
ls $PA_ROOT/default/dist/lib/rm-node*.jar

systemctl daemon-reload

if  ! $CONFLICT ; then
    if confirm "Do you want to start the scheduler service now? [Y/n] " ; then
        echo "If a problem occurs, check output in /var/log/proactive/scheduler"
        service proactive-scheduler start
    fi
fi
