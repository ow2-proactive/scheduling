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

import XenAPI
import sys
import re
import traceback
from subprocess import *

#XenServer helper class
#provides a wrapper for the XenAPI to make it more OO
#and provide pushData and getData as implemented in
#ProActive Virtual Machine Manager project
class XenServer_Helper :

    #Here are developpers data
    __proacRTKey = "PARuntimeKey."
    __proacHardwareAddress = "ha"
    proacHoldingVM = "holdingVM"
    proacRMUrl = "rmUrl"
    proacRMCreds = "credentials"
    proacNodeSourceName = "nodesource"
    proacHostCapacity = "hostCapacity"
    proacVmCapcity = "vmCapacity"
    proacNodeNumber = "nodeNumber"
    proacDynamicPropertyBaseStr = "dynamic."
    proacNodeURLBaseStr = "nodeUrl."
    def __init__(self,url,user,pwd):
        self.url = url
        self.user = user
        self.pwd = pwd
        self.session = XenAPI.Session(self.url)
        self.session.xenapi.login_with_password(self.user, self.pwd)

    def __del__(self):
        self.session.xenapi.session.logout()

    def __getMacAddress(self):
        """getMacAddress returns an array filled with every
        detected NIC's mac address on the current computer"""
        proc = None
        res = []
        if sys.platform == 'win32':
            proc = Popen( args = "ipconfig /all", stdout = PIPE, stderr = PIPE)
        else:
            proc = Popen( args = "/sbin/ifconfig", stdout = PIPE, stderr = PIPE)
        output = proc.communicate()[0]
        try:
            output = output.decode()
        except Exception:
            print("An error occurred while decoding mac address string")
            traceback.print_exc()
        tmp = re.findall("(?:[0-9a-fA-F]{2}[-:]){5}[0-9a-fA-F]{2}",output)
        for i, mac in enumerate(tmp):
            res.append(mac.replace("-", ":"))
        return res

    def __fixHoldingVirtualMachine(self):
        """fix the current holding virtual machine
        field given to the mac address"""

        vms = self.session.xenapi.VM.get_all()
        macs = self.__getMacAddress()
        print("vms:",vms)
        print("macs:",macs)
        url = None
        for i, vm in enumerate(vms):
            print("vm:",vm)
            data = self.session.xenapi.VM.get_xenstore_data(vm)
            for j,mac in enumerate(macs):
                print("mac:",mac)
                print("data:",data)
                key = XenServer_Helper.__proacRTKey + XenServer_Helper.__proacHardwareAddress
                try:
                    remoteMac = data[key].lower().strip()
                    mac = mac.lower().strip()
                    if remoteMac.startswith(mac) and remoteMac.endswith(mac):
                        self.holdingVM = vm
                        print ("holding vm: " + self.holdingVM)
                        return True
                except KeyError:
                    print ("invalid key: ", key, " supplied.")
        print("No holdingVM found")
        return False

    def __getHoldingVM(self):
        res = self.__fixHoldingVirtualMachine()
        if res == True:
            return self.holdingVM
        else:
            return None

    def getData(self,key):
        vm = self.__getHoldingVM()
        if vm == None:
            raise EnvironmentError("No holding VM found")
        datas = self.session.xenapi.VM.get_xenstore_data(self.holdingVM)
        try:
            data = datas[ XenServer_Helper.__proacRTKey + key ]
            return data
        except KeyError:
            print("Unable to get data " + key)
            traceback.print_exc()
            return None

    def pushData(self,key,value):
        vm = self.__getHoldingVM()
        if vm == None:
            raise EnvironmentError("No holding VM found")
        self.session.xenapi.VM.remove_from_xenstore_data(self.holdingVM,XenServer_Helper.__proacRTKey + key)
        self.session.xenapi.VM.add_to_xenstore_data(self.holdingVM,XenServer_Helper.__proacRTKey + key,value)
