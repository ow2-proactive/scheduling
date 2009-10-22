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

import sys
import rm_virtualization_lib
from subprocess import *

#Abstract_Runtime implementation for Sun xVM Virtualbox product.
#Be sure to have the guest additions installed on the virtual machine
#and add VBoxControl tool to the application path.
#This class is in charge of retrieving RM Node startup information.
class Virtualbox_Runtime ( rm_virtualization_lib.Abstract_Runtime ):

    #you can change this vector to specify a different environment
    __getRMUrl = ["VBoxControl","guestproperty","get","rmUrl"]
    __getRMCreds = ["VBoxControl","guestproperty","get","credentials"]
    __getNodeSourceName = ["VBoxControl","guestproperty","get", "nodesource"]
    __getHoldingVM = ["VBoxControl","guestproperty","get","holdingVM"]
    __getHostCapacity = ["VBoxControl","guestproperty","get","hostCapacity"]
    __getDynamicProperty = ["VBoxControl","guestproperty","get",""]
    __getDynamicPropertyBaseStr = "dynamic."
    __setNodeURL = ["VBoxControl","guestproperty","set","",""]
    __setNodeURLBaseStr = "nodeUrl."

    def isOk(self):
        """This method is used to check if the current environment
        matches vmware requirements to be used. If it is the case, and
        instance of VMware_Runtime will be returned"""
        try :
            proc = Popen(args = self.__getHoldingVM, stdout = PIPE, stderr = PIPE)
            output = repr(proc.communicate()[0])
            if output.find("No value set!") == -1:
                return self
            else :
                return None
        except :
            print ("An exception occured while testing VBoxControl")
            return None

    def start(self):
        """Exctracts required parameters and runs _start from
        AbstractRuntime"""
        #compulsory whatever the starting method choice is
        proc = Popen(args = self.__getHoldingVM, stdout = PIPE, stderr = PIPE)
        holdingVM = proc.communicate()[0].decode("utf-8").partition("Value: ")[2].strip().rstrip()
        proc = Popen(args = self.__getHostCapacity, stdout = PIPE, stderr = PIPE)
        hostCapacity = proc.communicate()[0].decode("utf-8").partition("Value: ")[2].strip().rstrip()
        #optional properties
        proc = Popen(args = self.__getHostCapacity, stdout = PIPE, stderr = PIPE)
        hostCapacity = proc.communicate()[0].decode("utf-8").partition("Value: ")[2].strip().rstrip()
        varI = 0
        propList = []
        while 1 :
            propCmd = self.__getDynamicPropertyBaseStr + repr(varI)
            self.__getDynamicProperty[3] = propCmd
            print( self.__getDynamicProperty )
            proc = Popen(args = self.__getDynamicProperty, stdout = PIPE, stderr = PIPE)
            prop = proc.communicate()[0].decode("utf-8").partition("Value: ")[2].strip().lstrip()
            print( prop )
            if prop == "null" :
                break
            else :
                propList.append(prop)
            varI = varI + 1
        #Get required information to connect to resource manager
        proc = Popen(args = self.__getRMUrl, stdout = PIPE, stderr = PIPE)
        rmUrl = proc.communicate()[0].decode("utf-8")
        if rmUrl.find("Value:") != -1:
            rmUrl = rmUrl.partition("Value: ")[2].strip().rstrip()
            proc = Popen(args = self.__getRMCreds, stdout = PIPE, stderr = PIPE)
            rmCreds = proc.communicate()[0].decode("utf-8").partition("Value: ")[2].strip().rstrip()
            proc = Popen(args = self.__getNodeSourceName, stdout = PIPE, stderr = PIPE)
            nodesourceName = proc.communicate()[0].decode("utf-8").partition("Value: ")[2].strip().rstrip()
            self._start(rmUrl = rmUrl, creds = rmCreds, nodesourceName = nodesourceName, holdingVM = holdingVM,\
                        hostCapacity = int(hostCapacity), propList = propList)
        else:
            self._start(rmUrl = None, creds = None, nodesourceName = None, holdingVM = holdingVM, \
                        hostCapacity = int(hostCapacity), propList = propList)

    def _writeNodeURL(self,url,nodeUrlID):
        """In charge of writing the node's url within
        the virtual machine's configuration file"""
        urlCmd = self.__setNodeURLBaseStr + repr(nodeUrlID)
        self.__setNodeURL[3] = urlCmd
        self.__setNodeURL[4] = url
        proc = Popen(args = self.__setNodeURL, stdout = PIPE, stderr = PIPE)
        ios = proc.communicate()
        out = ios[0].decode("utf-8").strip().lstrip()
        err = ios[1].decode("utf-8").strip().lstrip()
        print(repr(self.__setNodeURL) + " execution:")
        print("\tVBoxControl output: " + out)
        print("\tVBoxControl errput: " + err)
