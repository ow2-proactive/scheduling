#!/bin/sh
#
# init script for a Java application
#

# Check the application status
#
# This function checks if the application is running
if [[ $(id -u) -ne 0 ]] ; then echo "Please run the installation script as root" ; exit 1 ; fi

cd ..
cwd=$(pwd)

check_status() {

  # Running ps with some arguments to check if the PID exists 
  s=$(ps aux | grep '/bin/java.*calendar-service' | grep -v 'grep' | awk '{print $2}')

  # If somethig was returned by the ps command, this function returns the PID
  if [ $s ] ; then
    echo $s
    exit 1
  fi

  # In any another case, return 0
  echo 0

}

# Starts the application
start() {

  pid=$(check_status)

  if [ $pid -ne 0 ] ; then
    echo "Calendar service is already started"
    exit 1
  fi

  # If the application isn't running, starts it
  echo " *** Starting Calendar Service *** "

  #copy scheduler users to radicale server
  loginFile="$cwd/config/authentication/login.cfg"
  rm -rf /etc/radicale/users
  touch /etc/radicale/users

  while IFS=':' read -r user password
  do
      htpasswd -bs /etc/radicale/users "$user" "$password"    
  done < "$loginFile"

  # Redirects default and error output to a log file
  java -Dpa.scheduler.home=$cwd -jar $cwd/tools/calendar-service*.jar
  echo " *** Calendar service gets started *** "
}

# Stops the application
stop() {

  # Like as the start function, checks the application status
  pid=$(check_status)

  if [ $pid -ne 0 ] ; then
    # Kills the application process
    echo " *** Stopping Calendar service *** "
    kill -9 $pid
    echo "OK"    
    exit 1
  fi

  echo "Calendar service is already stopped"
}

# Show the application status
status() {

  # The check_status function, again...
  pid=$(check_status)

  # If the PID was returned means the application is running
  if [ $pid -ne 0 ] ; then
    echo "Calendar service is started"
  else
    echo "Calendar service is stopped"
  fi

}

# Main logic, a simple case to call functions
case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  restart)
    stop
    start
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
esac

exit 0
