#!/bin/sh
#
# init script for a Java application
#

# Check the application status
#
# This function checks if the application is running

check_status() {

  # Running ps with some arguments to check if the PID exists 
  s=$(ps aux | grep 'radicale -d -S' | grep -v 'grep' | awk '{print $2}')

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
    echo "Radicale is already started"
  else
    # If the application isn't running, starts it
    echo " *** Starting Radicale server *** "

    cd ~/.config/radicale/log

    radicale -d -S

    echo " *** Radicale server started *** "
  fi
}

# Starts the application
startDebug() {

  pid=$(check_status)

  if [ "$pid" -ne 0 ] ; then
    echo "Radicale is already started"
  else
    # If the application isn't running, starts it
    echo " *** Starting Radicale server *** "

    cd ~/.config/radicale/log

    radicale -d -S -D

    echo " *** OK *** "
  fi
}

# Stops the application
stop() {

  # Like as the start function, checks the application status
  pid=$(check_status)

  if [ "$pid" -ne 0 ] ; then
    # Kills the application process
    echo " *** Stopping Radicale server *** "
    kill -9 $pid
    echo "OK"
  else
    echo "Radicale server is already stopped"
  fi
}

# Stops the application
restart() {

  # Like as the start function, checks the application status
  pid=$(check_status)

  if [ "$pid" -ne 0 ] ; then
    # Kills the application process
    echo " *** Stopping Radicale server *** "
    kill -9 $pid
    echo "OK"
  else
    echo "Radicale server is already stopped"
  fi

  # If the application isn't running, starts it
  echo " *** Starting Radicale server *** "

  cd ~/.config/radicale/log

  radicale -d -S

  echo " *** OK *** "
}

# Show the application status
status() {

  # The check_status function, again...
  pid=$(check_status)

  # If the PID was returned means the application is running
  if [ "$pid" -ne 0 ] ; then
    echo "Radicale is started"
  else
    echo "Radicale is stopped"
  fi

}

# Main logic, a simple case to call functions
case "$1" in
  start)
    start
    ;;
  startDebug)
    startDebug
    ;;
  stop)
    stop
    ;;
  status)
    status
    ;;
  restart)
    restart
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status|startDebug}"
    exit 1
esac

exit 0