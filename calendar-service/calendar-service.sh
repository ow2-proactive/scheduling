#!/bin/sh
#
# init script for a Java application
#

# Check the application status
#
# This function checks if the application is running

script_dir=$(dirname "$0")
cwd="$script_dir/.."

check_status() {

  # Running ps with some arguments to check if the PID exists 
  s=$(ps aux | grep 'java.*calendar-service' | grep -v 'grep' | awk '{print $2}')

  # If somethig was returned by the ps command, this function returns the PID
  if [ "$s" ] ; then
    echo $s
    exit 1
  fi

  # In any another case, return 0
  echo 0

}

# Starts the application
start() {

  pid=$(check_status)

  if [ "$pid" -ne 0 ] ; then
    echo "Calendar service is already started"
  else
    # If the application isn't running, starts it
    echo " *** Starting Calendar Service *** "

    #copy scheduler users to radicale server
    loginFile="$cwd/config/authentication/login.cfg"

    cat "$loginFile" > ~/.config/radicale/users

    # Redirects default and error output to a log file
    java -Dpa.scheduler.home="$cwd" -Dspring.config.location="$cwd/config/calendar-service/application.properties" -jar "$cwd"/calendar-service/calendar-service*.jar > /dev/null 2>&1 &
    echo " *** Calendar service gets started *** "
  fi
}

# Stops the application
stop() {

  # Like as the start function, checks the application status
  pid=$(check_status)

  if [ "$pid" -ne 0 ] ; then
    # Kills the application process
    echo " *** Stopping Calendar service *** "
    kill -9 $pid
    echo "OK" 
  else
    echo "Calendar service is already stopped"
  fi
}

# Show the application status
status() {

  # The check_status function, again...
  pid=$(check_status)

  # If the PID was returned means the application is running
  if [ "$pid" -ne 0 ] ; then
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
  *)
    echo "Usage: $0 {start|stop|status}"
    exit 1
esac

exit 0
