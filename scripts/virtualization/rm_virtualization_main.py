#################################################################
#
# ProActive Parallel Suite(TM): The Java(TM) library for
#    Parallel, Distributed, Multi-Core Computing for
#    Enterprise Grids & Clouds
#
# Copyright (C) 1997-2011 INRIA/University of
#                 Nice-Sophia Antipolis/ActiveEon
# Contact: proactive@ow2.org or contact@activeeon.com
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License
# as published by the Free Software Foundation; version 3 of
# the License.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this library; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
# USA
#
# If needed, contact us to obtain a release under GPL Version 2 or 3
# or a different license than the AGPL.
#
#  Initial developer(s):               The ActiveEon Team
#                        http://www.activeeon.com/
#  Contributor(s):
#
#################################################################
# $$ACTIVEEON_INITIAL_DEV$$
#

#Main entry point to start RM Node to use in a virtualized environment.

import sys
import os
import time
pathname = os.path.dirname(sys.argv[0])
#updating lib path
sys.path.append(os.path.abspath(pathname + os.path.sep +  "lib"))
import rm_virtualization_lib
#updating hyperv path
sys.path.append(os.path.abspath(pathname + os.path.sep + "hyperv"))
import hyperv
#updating vmware path
sys.path.append(os.path.abspath(pathname + os.path.sep + "vmware"))
import vmware
#updating virtualbox path
sys.path.append(os.path.abspath(pathname + os.path.sep + "virtualbox"))
import virtualbox
#updating xenserver path
if sys.version_info[0] == 2 :
    sys.path.append(os.path.abspath(pathname + os.path.sep + "xenserver"))
    sys.path.append(os.path.abspath(pathname + os.path.sep + "xenserver" + os.path.sep + "ext"))
    import xenserver

if len(sys.argv) >= 2 :
    logFile = sys.argv[1]
    print ("logging on ",logFile)
    out = open(logFile,"a")
    sys.stdout = out
    sys.stderr = out
else :
    print ("logging on standard output")
print ("Begining log: ", time.strftime('%d/%m/%y %H:%M',time.localtime()))
x = None

#you can register new Abstract_Runtime implementation here
rm_virtualization_lib.Abstract_Runtime.addProvider(hyperv.HyperV_Runtime())
rm_virtualization_lib.Abstract_Runtime.addProvider(vmware.VMware_Runtime())
rm_virtualization_lib.Abstract_Runtime.addProvider(virtualbox.Virtualbox_Runtime())
if sys.version_info[0] == 2 :
    rm_virtualization_lib.Abstract_Runtime.addProvider(xenserver.XenServer_Runtime())

#iterates registered Abstract_Runtime implementation to find the good
#environment.
while x == None:
    x = rm_virtualization_lib.Abstract_Runtime.getInstance()
    if x == None :
        print ("getInstance from Abstract_Runtime returned None.")
        print ("Waiting 10s...")
        time.sleep(10)
x.start()
