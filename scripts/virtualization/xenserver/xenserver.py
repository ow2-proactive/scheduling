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

import rm_virtualization_lib
from rm_virtualization_xenserver import XenServer_Helper
import traceback
from subprocess import *

#you have to set the following info to be able to
#register Resource Manager nodes
xenServerAddress = "http://xenserver.address"
xenServerUserID = "user"
xenServerUserPWD = "pwd"

#Implementation of Abstract_Runtime for the XenServer virtualization software
class XenServer_Runtime( rm_virtualization_lib.Abstract_Runtime ):

    def isOk(self):
        """This method is used to check if the current environment
        matches xenserver requirements to be used. If it is the case, an
        instance of VMware_Runtime will be returned"""
        try :
            self.helper = XenServer_Helper(xenServerAddress,xenServerUserID,xenServerUserPWD)
            holdingVM = self.helper.getData(XenServer_Helper.proacHoldingVM)
            if holdingVM != None :
                return self
            else :
                return None
        except :
            print ("An exception occured while testing xenserver env")
            traceback.print_exc()
            return None

    def start(self):
        """Exctracts required parameters and runs _start from
        AbstractRuntime"""
        #compulsory whatever the starting method choice is
        holdingVM = self.helper.getData(XenServer_Helper.proacHoldingVM)
        print( XenServer_Helper.proacHoldingVM )
        print( holdingVM )
        hostCapacity = self.helper.getData(XenServer_Helper.proacHostCapacity)
        print( XenServer_Helper.proacHostCapacity )
        print( hostCapacity )
        varI = 0
        propList = []
        while 1 :
            propKey = XenServer_Helper.proacDynamicPropertyBaseStr + repr(varI)
            prop = self.helper.getData(propKey)
            print( propKey )
            print( prop )
            if prop == "null" :
                break
            else :
                propList.append(prop)
            varI = varI + 1
        #Get required information to connect to resource manager
        rmUrl = self.helper.getData(XenServer_Helper.proacRMUrl)
        print( XenServer_Helper.proacRMUrl )
        print( rmUrl )
        if rmUrl is not None and rmUrl.find("://") != -1:
            rmCreds = self.helper.getData(XenServer_Helper.proacRMCreds)
            print( XenServer_Helper.proacRMCreds )
            print( rmCreds )
            nodesourceName = self.helper.getData(XenServer_Helper.proacNodeSourceName)
            print( XenServer_Helper.proacNodeSourceName )
            print( nodesourceName )
            self._start(rmUrl = rmUrl, creds = rmCreds, nodesourceName = nodesourceName, holdingVM = holdingVM,\
                        hostCapacity = int(hostCapacity), propList = propList)
        else:
            self._start(rmUrl = None, creds = None, nodesourceName = None, holdingVM = holdingVM, \
                        hostCapacity = int(hostCapacity), propList = propList)

    def _writeNodeURL(self,url,nodeUrlID):
        """In charge of writing the node's url within
        the virtual machine's configuration file"""
        urlKey = XenServer_Helper.proacNodeURLBaseStr + repr(nodeUrlID)
        self.helper.pushData(urlKey,url)
