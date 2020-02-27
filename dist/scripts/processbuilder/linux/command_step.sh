#! /bin/bash
# last step of the command launching mechanism
# will test if a command can or can not be launched by the current user
# If everything seems OK it will signal the java process builder on stdout and
# stderr that it will proceed with executing the user command

# parameters:
#	$1 - the token
#	$2 - temp file to save exit value
#	$3 - temp file containing the environment
# 	$4 - absolute path to the working dir of user command
#	$5... - command to execute

# IMPORTANT: On error messages refer to the JavaDoc of OSProcessBuilder

# defining here the 'tokens' used in the java process builder
OSPL_E_PREFIX="_OS_PROCESS_LAUNCH_ERROR_";
OSPL_E_CAUSE="CAUSE";
OSLP_PACKAGE="org.objectweb.proactive.extensions.processbuilder.exception."
#---------------

trap "echo trapped signal" SIGTERM

token=$1

# temp file
tmp=$2

if [ ! -e $tmp ]; then
  # if the temp file is not OK
  error="$OSPL_E_PREFIX ${OSLP_PACKAGE}FatalProcessBuilderException $OSPL_E_CAUSE Could not access temp file for storing the return value!";
  echo $error 1>&2;
  exit 1;
fi;

# temp env file
tmpenv=$3

if [ ! -e $tmpenv ]; then
  # if the temp file is not OK
  error="$OSPL_E_PREFIX ${OSLP_PACKAGE}FatalProcessBuilderException $OSPL_E_CAUSE Could not access temp environment file for loading system variables!";
  echo $error 1>&2;
  exit 1;
fi;

# if we have data in the file, then it is the dump of the environment
# PROACTIVE-970 : default env is used
#if [ -s $tmp ]; then
#  source $tmp
#fi;

# working directory for the user command
workdir="$4"

# losing the first four arguments, all that remains is the user command
shift;shift;shift;shift;

# change umask settings to prevent file permission issues in RunAsMe mode
umask 0000

export XDG_RUNTIME_DIR=/run/user/$UID

# check if the workdir is OK
if [ -d "${workdir}" ]; then
  cd "$workdir"
else
  error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=2, No such directory (${workdir}) ";
  echo $error 1>&2;
  exit 1;
fi;

# see if the command itself is executable (there really is a program to run)
cmd_path=`which "$1"`;
if [ "$?" != "0" ]; then
  error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=2, No such file or directory ";
  echo $error 1>&2;
  exit 1;
fi;

# TODO: maybe test in more detail the access rights to the exec

if [ -e "$cmd_path" ]; then
  if [ -x "$cmd_path" ]; then
    # load the user default environment
    if [ -f "$HOME/.bashrc" ]; then
        source "$HOME/.bashrc"
    fi

    # load the custom environment variables
    set -a
    source $tmpenv
    rm $tmpenv 2> /dev/null

    # let's tell the launcher that everything is OK
    confirm="_OS_PROCESS_LAUNCH_INIT_FINISHED_"
    echo $confirm;
    echo $confirm 1>&2;

    export token=$token
    # execute it!
    (exec -a "$token" "$@")


    # write return value to the temp file
    error=$?
    echo $error > $tmp;
    exit 0;
  else
    error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=13, Permission denied ";
    echo $error 1>&2;
    exit 1;
  fi;
else
  error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=2, No such file or directory ";
  echo $error 1>&2;
  exit 1;
fi;
