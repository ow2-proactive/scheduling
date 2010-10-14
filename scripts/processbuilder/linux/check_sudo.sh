# script to test if we can run as a given user
# it works both with passwordless and password-enabled user changing

# arguments:
#	$1 - username
#	$2 - password in plaintext (optional)
#	$3 - scripts home folder (needed only if we also have a password)

if [ "$2" == "" ]; then
  echo 0 | sudo -Su "$1" whoami
else
  # check if we are running on a 64bit arch, or a 32bit one.
  # The only difference between the 'suer' executables is their
  # target architecture used at compilation time.
  if [[ `uname -i` == *64* ]];
  then
    if [ ! -e "$3/suer64" ];
    then
      error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=2, No such file (${3}/suer64) ";
      echo $error 1>&2;
      exit 1;
    fi
    "$3/suer64" "$1" "$2" whoami;
  else
    if [ ! -e "$3/suer32" ];
    then
      error="$OSPL_E_PREFIX java.io.IOException $OSPL_E_CAUSE error=2, No such file (${3}/suer32) ";
      echo $error 1>&2;
      exit 1;
    fi
    "$3/suer32" "$1" "$2" whoami;
  fi
  ###### DEVELOPER NOTE:
  #	In case the 'suer' solution does not meet all requirements, it is possible to conveniently replace
  #	it with a solution built on the Expect library. We need just a script which could interpret the 'su'
  #	messages. Since expect mixes together the error and output channels, a named pipe should be used to
  #	forward the error of the inner commands to the outside world. This named pipe could be created in the
  #	temp directory, and deleted when the scripts exit.
  ######
fi;

