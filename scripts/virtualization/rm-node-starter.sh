#!/bin/sh
#
# This is a basic dummy shell script to get your scheduling environment working
# It can be used on debian like distros. Have it registered using update-rc.d...
#
# Basic support for IRIX style chkconfig
# chkconfig: 235 99 99
# description: For resource manager node deployment

# Basic support for the Linux Standard Base Specification 1.0.0 (to be used by
# insserv for example)
### BEGIN INIT INFO
# Provides: ProActive
# Required-Start: VBoxControl vmware-guestd
# Required-Stop:
# Default-Start: 2 3 5
# Default-Stop: 0 1 6
# Description: Manages the services needed to run ProActive
### END INIT INFO

# BEGINNING_OF_UTIL_DOT_SH
#!/bin/sh
#
#
# Get lsb functions
. /lib/lsb/init-functions
. /etc/default/rcS

##################################################################
#
# ProActive: The Java(TM) library for Parallel, Distributed,
#            Concurrent computing with Security and Mobility
#
# Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
# Contact: proactive@ow2.org
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version
# 2 of the License, or any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
# USA
#
#  Initial developer(s):               The ActiveEon Team
#                        http://www.activeeon.com/
#  Contributor(s):
#
#
##################################################################
# $$ACTIVEEON_INITIAL_DEV$$


PIDFILE=/var/run/rm-node-runtime.pid
LOGFILE=/var/log/rm-node-runtime.log
#change the path of your python & Scheduling apps here
#to avoid python path issues, supply an absolute path
EXE="/path/to/python /path/to/Scheduler/dist/scripts/virtualization/rm_virtualization_main.py ${LOGFILE}"
ARGS=

case "$1" in
  start)
	#if you want to update your Scheduler dist directory fix the line below
	#rsync -avz -e ssh login@host.domain.com:/path/to/Scheduling/directory /usr/local/share/Scheduling
	[ -e ${LOGFILE} ] || touch ${LOGFILE}
    log_begin_msg "Starting ProActive RM Node..."
	start-stop-daemon --start --exec ${EXE} -b -m --pidfile ${PIDFILE} -- ${ARGS}
    log_end_msg $?
    ;;
  stop)
    log_begin_msg "Stopping ProActive RM Node..."
    start-stop-daemon --stop -p ${PIDFILE}
    log_end_msg $?
    ;;
  restart)
    $0 stop
    sleep 1
    $0 start
    ;;
  *)
    log_success_msg "Usage: /etc/init.d/rm-node-starter.sh {start|stop|restart}"
    exit 1
esac

exit 0
