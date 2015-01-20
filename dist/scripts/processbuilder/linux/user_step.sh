#! /bin/bash
# This script will try to proceed with the next step of the launching mechanism, i.e. 
# it will SUDO the command_step.
#
# If it fails, it will write an error message to the stderr and exit with error code 0.
# The error code however is not relevant, because the JAVA process launcher will interpret the 
# error immediately, and ignore any follow-up

# parameters:
#	$1 - token
#	$2 - temp file for the return value of the user command
#	$3 - working dir for user command (absolute)
# 	$4 - user name
#	$5... - command to execute

# IMPORTANT: On error messages refer to the JavaDoc of OSProcessBuilder

# defining here the 'tokens' used in the java process builder
OSPL_E_PREFIX="_OS_PROCESS_LAUNCH_ERROR_";
OSPL_E_CAUSE="CAUSE";
OSLP_PACKAGE="org.objectweb.proactive.extensions.processbuilder.exception."
#---------------

token=$1 

# temp file
tmp=$2

# user command's working dir
workdir=$3

# user name
usr=$4

# simulating password input, in order not to get blocked in case "sudo -u root" happens for instance.
# also, we shift the first 4 parameters; this way $@ will contain only the user command
shift; shift; shift; shift;

passw=$PA_OSPB_USER_PASSWORD
keycont=$PA_OSPB_USER_KEY_CONTENT

export PA_OSPB_USER_PASSWORD=***
export PA_OSPB_USER_KEY_CONTENT=***

if [ "$passw" == "" ]; then 
  if [ "$keycont" == "" ]; then 
    # use sudo
    echo 0 | sudo -ESHu $usr `pwd`/command_step.sh $token $tmp "$workdir" "$@"
    exitc=$?
  else 
    # use ssh
    # PROACTIVE-970 : default env is used
    # Note that tmp is still used fo return value
    # export >> $tmp;

    OLD_UMASK=`umask`
    umask 0177
    keyfile=`mktemp  2>/dev/null || mktemp  -t 'mytmpkeyfile'`
    umask $OLD_UMASK
    echo "$keycont" > $keyfile

    for i in "$@" 
      do
      args="${args} ""'"${i//\'/\'\"\'\"\'}"'"
    done
    ssh -n -o PasswordAuthentication=no -o StrictHostKeyChecking=no -i $keyfile $usr@localhost `pwd`/command_step.sh $token $tmp "$workdir" $args
    exitc=$?
    rm $keyfile
  fi;
else
  # if we use the 'suer' than since it will write the command to the shell as text, we have to make
  # sure that our command will always be wellformed. Thus, we write it out to environment variables
  # and pass their names to the executable. We actually pass the environment variables complete with 
  # dollar sign, so when they get written to the bash, they will be replaced with their values automatically
  env_var_prefix="PA_OSPB_CMD_ARG_NO_"
  cnt=1
  for i in "$@" 
  do 
    export ${env_var_prefix}${cnt}="$i"
    args="${args} "\"""\$"$env_var_prefix$cnt"\"
    cnt=`expr $cnt + 1`
  done

  # check if we are running on a 64bit arch, or a 32bit one. 
  # The only difference between the 'suer' executables is their
  # target architecture used at compilation time.
  if [[ `uname -m` == *64* ]];
  then
    if [[ `uname -s` == *Darwin* ]];
    then
      # fallback on another suer for Mac
      echo "$passw" | ./suermac64 $usr ./command_step.sh $token $tmp "$workdir" $args;
    else
      echo "$passw" | ./suer64 $usr ./command_step.sh $token $tmp "$workdir" $args;
    fi
    exitc=$?
  else
    echo "$passw" | ./suer32 $usr ./command_step.sh $token $tmp "$workdir" $args;
    exitc=$?
  fi  
  ###### DEVELOPER NOTE:
  #	In case the 'suer' solution does not meet all requirements, it is possible to conveniently replace
  #	it with a solution built on the Expect library. We need just a script which could interpret the 'su'
  #	messages. Since expect mixes together the error and output channels, a named pipe should be used to 
  #	forward the error of the inner commands to the outside world. This named pipe could be created in the
  #	temp directory, and deleted when the scripts exit.
  ######
fi;

# sudo should exit with error code 1 only in case it was unsuccessful
# the command which is executed inside will pass its return value through a tempfile
if [ $exitc != 0 ]; then
  error="$OSPL_E_PREFIX ${OSLP_PACKAGE}OSUserException $OSPL_E_CAUSE Cannot execute as user $usr! (Code=$exitc) token:$token tmp:$tmp user:$usr";
  echo $error 1>&2;
  exit 1;
fi;
  
