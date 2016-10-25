#!/usr/bin/env bash

if [[ $(id -u) -ne 0 ]] ; then echo "Please run the installation script as root" ; exit 1 ; fi

SCRIPT_DIR="$( cd "$(dirname "$0")" ; pwd -P )"

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

echo "This will uninstall all ProActive Scheduler's installation on the current machine."

if confirm "Do you want to continue? [y/N] " "N" ; then
    read -e -p "Root directory where the ProActive Scheduler is installed: " -i "/opt/proactive" PA_ROOT
    PA_ROOT=$(echo $PA_ROOT | xargs)
    if [ -d $PA_ROOT ]; then
        if [ -f /etc/init.d/proactive-scheduler ]; then
            service proactive-scheduler stop
        fi

        USER=$( stat -c %U $PA_ROOT )

        rm -rf /opt/proactive
        rm -f /etc/init.d/proactive-scheduler
        rm -f /var/log/proactive/scheduler

        echo "Directory was associated with user : $USER"
        if confirm "Do you want to delete this user? [Y/n] "  ; then
            userdel "$USER"
        fi

        echo "ProActive Scheduler de-installed from $PA_ROOT."
    else
        echo "$PA_ROOT is not a directory."
        exit 1
    fi
fi
