#! /bin/bash
# script to test if we can run as a given user
# it works both with passwordless and password-enabled user changing

# arguments:
#	$1 - username
#	$2 - scripts home folder (needed only if we also have a password)
username=$1
passw=$PA_OSPB_USER_PASSWORD
keycont=$PA_OSPB_USER_KEY_CONTENT

export PA_OSPB_USER_PASSWORD=***
export PA_OSPB_USER_KEY_CONTENT=***

if [ "$passw" == "" ]; then
  if [ "$keycont" == "" ]; then
    echo 0 | sudo -Su "$username" whoami
  else
    # use ssh
    keyfile=`mktemp  2>/dev/null || mktemp  -t 'mytmpfile'`
    echo "$keycont" > $keyfile
    chmod 400 $keyfile
    echo $keyfile $username
    ssh -n -i $keyfile $username@localhost whoami
    if [ $? == 0 ]; then
      echo $usr
    else 
      echo FAIL
    fi;
    rm -f $keyfile
    exit 0;
  fi;
else 
  # check if we are running on a 64bit arch, or a 32bit one. 
  # The only difference between the 'suer' executables is their
  # target architecture used at compilation time.
  if [[ `uname -m` == *64* ]];
  then
    if [ ! -e "$2/suer64" ];
    then 
      error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=2, No such file (${2}/suer64) ";
      echo $error 1>&2;
      exit 1;
    fi
    echo "$passw" | "$2"/suer64 $username whoami
  else
    if [ ! -e "$2/suer32" ];
    then 
      error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=2, No such file (${2}/suer32) ";
      echo $error 1>&2;
      exit 1;
    fi
    echo "$passw" | "$2"/suer32 $username whoami
  fi  
  ###### DEVELOPER NOTE:
  #	In case the 'suer' solution does not meet all requirements, it is possible to conveniently replace
  #	it with a solution built on the Expect library. We need just a script which could interpret the 'su'
  #	messages. Since expect mixes together the error and output channels, a named pipe should be used to 
  #	forward the error of the inner commands to the outside world. This named pipe could be created in the
  #	temp directory, and deleted when the scripts exit.
  ######
fi;

