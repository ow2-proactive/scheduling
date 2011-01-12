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

import rm_virtualization_lib
from rm_virtualization_hyperv import *
import traceback
from subprocess import *

#you have to set the following info to be able to
#register Resource Manager nodes
hypervServerAddress = None
hypervServerUserID = None
hypervServerUserPWD = None

#Implementation of Abstract_Runtime for the XenServer virtualization software
class HyperV_Runtime( rm_virtualization_lib.Abstract_Runtime ):

    def isOk(self):
        """This method is used to check if the current environment
        matches hyperv requirements to be used. If it is the case, an
        instance of VMware_Runtime will be returned"""
        try :
            self.helper = HyperV_Helper_Factory.getHyperV_Helper_instance(hypervServerAddress,hypervServerUserID,hypervServerUserPWD)
            holdingVM = self.helper.getData(HyperV_Helper.proacHoldingVM)
            if holdingVM != None :
                return self
            else :
                return None
        except :
            print ("An exception occured while testing hyperv env")
            traceback.print_exc()
            return None

    def start(self):
        """Exctracts required parameters and runs _start from
        AbstractRuntime"""
        #compulsory whatever the starting method choice is
        holdingVM = self.helper.getData(HyperV_Helper.proacHoldingVM)
        print( HyperV_Helper.proacHoldingVM )
        print( holdingVM )
        hostCapacity = self.helper.getData(HyperV_Helper.proacHostCapacity)
        print( HyperV_Helper.proacHostCapacity )
        print( hostCapacity )
        varI = 0
        propList = []
        while 1 :
            propKey = HyperV_Helper.proacDynamicPropertyBaseStr + repr(varI)
            prop = self.helper.getData(propKey)
            print( propKey )
            print( prop )
            if prop == "null" :
                break
            else :
                propList.append(prop)
            varI = varI + 1
        #Get required information to connect to resource manager
        rmUrl = self.helper.getData(HyperV_Helper.proacRMUrl)
        print( HyperV_Helper.proacRMUrl )
        print( rmUrl )
        if rmUrl is not None and rmUrl.find("://") != -1:
            rmCreds = self.helper.getData(HyperV_Helper.proacRMCreds)
            print( HyperV_Helper.proacRMCreds )
            print( rmCreds )
            nodesourceName = self.helper.getData(HyperV_Helper.proacNodeSourceName)
            print( HyperV_Helper.proacNodeSourceName )
            print( nodesourceName )
            self._start(rmUrl = rmUrl, creds = rmCreds, nodesourceName = nodesourceName, holdingVM = holdingVM,\
                        hostCapacity = int(hostCapacity), propList = propList)
        else:
            self._start(rmUrl = None, creds = None, nodesourceName = None, holdingVM = holdingVM, \
                        hostCapacity = int(hostCapacity), propList = propList)

    def _writeNodeURL(self,url,nodeUrlID):
        """In charge of writing the node's url within
        the virtual machine's configuration file"""
        urlKey = HyperV_Helper.proacNodeURLBaseStr + repr(nodeUrlID)
        self.helper.pushData(urlKey,url)
