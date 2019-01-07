#!/usr/bin/env bash

if [ "$#" -ne 2 ]; then
    SCRIPT_NAME=`basename "$0"`
    echo "$SCRIPT_NAME"
    echo "Can be used to patch a ProActive installation git versioning when migrating from a version prior to 8.4.0 to 8.4.0 or above."
    echo "Please specify a ProActive installation path and a folder containing the original extracted archive corresponding to your version"
    echo "This command must run as the user owning the ProActive installation directory."
    echo ""
    echo "The command will delete all the git history and create a new one, based on the differences between your"
    echo "ProActive installation and the default configuration files of the same ProActive version."
    echo ""
    echo "Usage:"
    echo "./patch_git_prior_8_4.sh <proactive_installation_to_patch_path> <original_extracted_archive_path>"
    exit 1
fi
PA_DIR="$(readlink -f "$1")"
ORIGINAL_DIR=$2
PA_FOLDER_NAME="$(basename "$PA_DIR")"

if [ "$(stat -c "%U" "$PA_DIR")" != "$(whoami)" ] ; then echo "Please run the patch script as the owner of $PA_DIR" ; exit 1 ; fi

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

staging_changes()
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


copy_configs()
{
    ORIGIN="$1"
    DESTINATION="$2"
    OLD_PWD=$(pwd)
    cd "$ORIGIN"
    cp --parents -R -f "config" "$DESTINATION"/

    for file in $( find dist/war -name 'application.properties' ); do
        cp --parents -R -f "$file" "$DESTINATION"/
    done


    cp --parents -R -f dist/war/rm/rm.conf "$DESTINATION"/
    cp --parents -R -f dist/war/scheduler/scheduler.conf "$DESTINATION"/
    cp --parents -R -f bin/proactive-server "$DESTINATION"/
    cp --parents -R -f tools/proactive-scheduler "$DESTINATION"/
    cp --parents -R -f tools/*.groovy "$DESTINATION"/
    cd "$OLD_PWD"
}

echo "This command will patch the ProActive installation in $PA_DIR using the unmodified same version package in $ORIGINAL_DIR. The original configuration files and git history are stored in an archive file for backup purpose."
if confirm "Proceed? [Y/n] "; then

    echo "Zipping current content of config folder to $(pwd)/config.zip."
    echo "If any problem occurs, in order to restore the previous state, please follow the following steps:"
    echo " 1) unzip the config.zip file into $PA_DIR/"
    echo " 2) delete the folder $PA_DIR/.git"
    echo " 3) make sure all file belong to the correct user and not to root"
    echo ""
    echo "Beginning the patch procedure..."

    OLD_PWD=$(pwd)

    cd "$PA_DIR"

    zip -rq config.zip "config"

    echo "Removing previous git history in $PA_DIR/config"

    rm -rf "$PA_DIR/config/.git"

    PATCH_DIR=/tmp/pa_patch

    mkdir -p "$PATCH_DIR"

    cd "$PA_DIR"

    copy_configs "$PA_DIR" "$PATCH_DIR"

    copy_configs "$ORIGINAL_DIR" "$PA_DIR"

    init_and_ignores

    staging_changes

    git commit -m "Initial configuration files for $PA_FOLDER_NAME"
    git tag -f -a "root" -m "Root commit"

    copy_configs "$PATCH_DIR" "$PA_DIR"

    staging_changes

    git commit -m "Modifications in configuration files for $PA_FOLDER_NAME"

    cd "$OLD_PWD"

    echo "Patch applied, you can verify the result of the patch by looking at the new git history:"
    echo "cd $PA_DIR"
    echo "git log"
    echo "... you can use git show to see individual commits"

fi
