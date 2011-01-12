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

import sys
import rm_virtualization_lib
from subprocess import *

#Abstract_Runtime implementation for VMware products.
#Ensure that the "vmware-guestd" program is in the application PATH ( and thus, that
#you have the vmware guest tools installed on the virtual machine.
#This class retrieves information using vmware-guestd to be able to start RM Node.
class VMware_Runtime ( rm_virtualization_lib.Abstract_Runtime ) :

    #you can change this vector to specify a different environment
    __getRMUrl = ["vmware-guestd","--cmd","info-get guestinfo.rmUrl"]
    __getRMCreds = ["vmware-guestd","--cmd","info-get guestinfo.credentials"]
    __getNodeSourceName = ["vmware-guestd","--cmd","info-get guestinfo.nodesource"]
    __getHoldingVM = ["vmware-guestd","--cmd","info-get guestinfo.holdingVM"]
    __getHostCapacity = ["vmware-guestd","--cmd","info-get guestinfo.hostCapacity"]
    __getVmCapcity = ["vmware-guestd","--cmd","info-get guestinfo.vmCapacity"]
    __getNodeNumber = ["vmware-guestd","--cmd","info-get guestinfo.nodeNumber"]
    __getDynamicProperty = ["vmware-guestd","--cmd",""]
    __getDynamicPropertyBaseStr = "info-get guestinfo.dynamic."
    __setNodeURL = ["vmware-guestd","--cmd",""]
    __setNodeURLBaseStr = "info-set guestinfo.nodeUrl."

    def isOk(self):
        """This method is used to check if the current environment
        matches vmware requirements to be used. If it is the case, and
        instance of VMware_Runtime will be returned"""
        try :
            proc = Popen(args = self.__getHoldingVM, stdout = PIPE, stderr = PIPE)
            output = repr(proc.communicate()[0])
            if output.find("No Value Found") == -1:
                return self
            else :
                return None
        except :
            print ("An exception occurred while testing vmware-guestd")
            return None

    def start(self):
        """Extracts required parameters and runs _start from
        AbstractRuntime"""
        #compulsory whatever the starting method choice is
        proc = Popen(args = self.__getHoldingVM, stdout = PIPE, stderr = PIPE)
        holdingVM = proc.communicate()[0].decode("utf-8").strip().rstrip()
        print( self.__getHoldingVM )
        print( holdingVM )
        proc = Popen(args = self.__getHostCapacity, stdout = PIPE, stderr = PIPE)
        hostCapacity = proc.communicate()[0].decode("utf-8").strip().rstrip()
        print( self.__getHostCapacity )
        print( hostCapacity )
        varI = 0
        propList = []
        while 1 :
            propCmd = self.__getDynamicPropertyBaseStr + repr(varI)
            self.__getDynamicProperty[2] = propCmd
            print( self.__getDynamicProperty )
            proc = Popen(args = self.__getDynamicProperty, stdout = PIPE, stderr = PIPE)
            prop = proc.communicate()[0].decode("utf-8").strip().lstrip()
            print( prop )
            if prop == "null" :
                break
            else :
                propList.append(prop)
            varI = varI + 1
        #Get required information to connect to resource manager
        proc = Popen(args = self.__getRMUrl, stdout = PIPE, stderr = PIPE)
        rmUrl = proc.communicate()[0].decode("utf-8").strip().rstrip()
        print( self.__getRMUrl )
        print( rmUrl )
        if rmUrl.find("://") != -1:
            proc = Popen(args = self.__getRMCreds, stdout = PIPE, stderr = PIPE)
            rmCreds = proc.communicate()[0].decode("utf-8").strip().rstrip()
            print( self.__getRMCreds )
            print( rmCreds )
            proc = Popen(args = self.__getNodeSourceName, stdout = PIPE, stderr = PIPE)
            nodesourceName = proc.communicate()[0].decode("utf-8").strip().rstrip()
            print( self.__getNodeSourceName )
            print( nodesourceName )
            self._start(rmUrl = rmUrl, creds = rmCreds, nodesourceName = nodesourceName, holdingVM = holdingVM,\
                        hostCapacity = int(hostCapacity), propList = propList)
        else:
            self._start(rmUrl = None, creds = None, nodesourceName = None, holdingVM = holdingVM, \
                        hostCapacity = int(hostCapacity), propList = propList)

    def _writeNodeURL(self,url,nodeUrlID):
        """In charge of writing the node's url within
        the virtual machine's configuration file"""
        urlCmd = self.__setNodeURLBaseStr + repr(nodeUrlID) + " " + url
        self.__setNodeURL[2] = urlCmd
        proc = Popen(args = self.__setNodeURL, stdout = PIPE, stderr = PIPE)
        ios = proc.communicate()
        out = ios[0].decode("utf-8").strip().lstrip()
        err = ios[1].decode("utf-8").strip().lstrip()
        print(repr(self.__setNodeURL) + " execution:")
        print("\tvmware-guestd output: " + out)
        print("\tvmware-guestd errput: " + err)
