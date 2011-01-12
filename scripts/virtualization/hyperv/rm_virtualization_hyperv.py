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
import os
import re
import traceback

#For data serialization, json import
if sys.version_info[0] == 2 and sys.version_info[1] < 6 :
    import simplejson as json
else:
    import json
#For Windows Guests, registry access
if sys.platform == 'win32':
    if sys.version_info[0] == 2:
        import _winreg as winreg
    if sys.version_info[0] == 3:
        import winreg
from subprocess import *
import socket

#A watch dog to end IPC
EOF_MARKER = "EOF"
PA_MESSAGE_LENGTH = 1000
SOCKET_TIMEOUT = 30

#HyperV_Helper factory class
class HyperV_Helper_Factory :

    def getHyperV_Helper_instance(url, user, pwd):
        """HyperV_Helper getter. The best suitable
        helper is returned. First tries to get windows
        helper as it doesn't require credentials and
        remote connection."""
        #_test method is not a template. That tightly couple
        #Factory with implementation
        if HyperV_Helper_Windows._test(url, user, pwd):
            print("HyperV_Helper_Windows instantiated")
            return HyperV_Helper_Windows(url, user, pwd)
        elif HyperV_Helper_NonWindows._test(url, user, pwd):
            print("HyperV_Helper_NonWindows instantiated")
            return HyperV_Helper_NonWindows(url, user, pwd)
        else:
            raise Exception("Cannot define HyperV_Helper_instance,\
                 specify url, user & pwd in hyperv.py or install guest tools")
    getHyperV_Helper_instance = staticmethod(getHyperV_Helper_instance)

#Parent HyperV_Helper class,
class HyperV_Helper:

    #To build class path for Java program/IPC
    scriptRoot = os.path.dirname(sys.argv[0])
    utilsClassPath = os.path.abspath(scriptRoot + os.path.sep + ".." + os.path.sep + ".."\
                                     + os.path.sep + "lib" + os.path.sep + "ProActive.jar")

    #Java program function for IPC
    utilsCommandHoldingVM = "holdingVM"
    utilsCommandGetData = "getData"
    utilsCommandPushData = "pushData"
    utilsSocketTimeout = 15

    #Java program
    utilsCommandEXE = "org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm.HyperVUtils"

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

    def __init__(self):
        self.config = {}

    #Used to retrieve a data set in virtual machine configuration
    def getData(self,key):
        """To get a data saved thanks to VirtualMachine2#pushData method
        belonging to ProActive Virtual Machine Management Project. One can
        also use native HyperV WMI api to push data. see
        Msvm_VirtualSystemManagementService#ModifyKvpItems method"""
        if self.initialized != None and self.initialized != True:
            self._fixHoldingVirtualMachine()
        socketServer = None
        try:
            tmp = self.config[key]
            if tmp != None and len(tmp) != 0:
                return tmp
        except Exception:
            print("key ", key, " not yet registered")
        try:
            socketServer = ProActiveSocketServer(2)
            command =  ["java", "-cp", HyperV_Helper.utilsClassPath, HyperV_Helper.utilsCommandEXE, repr(socketServer.getPort()), \
            self.url, self.user, self.pwd, HyperV_Helper.utilsCommandGetData, self.holdingVM, key]
            print ("Executing: ", repr(command))
            proc = Popen( args = command , stdout = PIPE, stderr = PIPE)
            result = None
            circuitBroker = 0
            while result == None or len(result) == 0 or circuitBroker < 2:
                circuitBroker = circuitBroker + 1
                result = socketServer.processChildComm()
                if result != None and len(result) > 0:
                    tmpDict = None
                    try:
                        tmpDict = json.loads(result[0])
                    except Exception:
                        tmpDict = json.loads(result[0].replace("\\", "\\\\"))
                    if tmpDict != None:
                        for i, tmpKey in enumerate(tmpDict.keys()):
                            self.config[tmpKey] = tmpDict[tmpKey]
                        return self.config[key]
                    else:
                        return ""
        finally :
            if socketServer != None:
                socketServer.clean()

    def pushData(self, key, value):
        """To push a key, value pair in this
        virtual machine configuration"""
        if self.initialized != None and self.initialized != True:
            self._fixHoldingVirtualMachine()
        socketServer = None
        try:
            socketServer = ProActiveSocketServer(2)
            command =  ["java", "-cp", HyperV_Helper.utilsClassPath, HyperV_Helper.utilsCommandEXE, repr(socketServer.getPort()), \
            self.url, self.user, self.pwd, HyperV_Helper.utilsCommandPushData, self.holdingVM, key, value]
            print ("Executing: ", repr(command))
            proc = Popen( args = command , stdout = PIPE, stderr = PIPE)
            result = None
            circuitBroker = 0
            while result == None or len(result) == 0 or circuitBroker < 2:
                circuitBroker = circuitBroker + 1
                result = socketServer.processChildComm()
        finally :
            if socketServer != None:
                socketServer.clean()

    #Tries to determine in which virtual machine we are running
    def _fixHoldingVirtualMachine(self):
        """Fix the current holding virtual machine
        field given the mac address. Connect to the
        owning hyper-v and iterates over all virtual
        machines to determine which one contains similar
        NIC"""
        socketServer = None
        try:
            macs = self.__getMacAddress()
            print("NIC's mac found: ",macs)
            socketServer = ProActiveSocketServer(2)
            command = ["java", "-cp", HyperV_Helper.utilsClassPath, HyperV_Helper.utilsCommandEXE, repr(socketServer.getPort()), \
            self.url, self.user, self.pwd, HyperV_Helper.utilsCommandHoldingVM]
            for i, mac in enumerate(macs):
                command.append(mac)
            print("Executing: ", repr(command))
            proc = Popen( args = command , stdout = PIPE, stderr = PIPE)
            result = None
            circuitBroker = 0
            while result == None or len(result) == 0 or circuitBroker < 2:
                result = socketServer.processChildComm()
                if result != None and len(result) > 0:
                    self.holdingVM = result[0]
                    self.config[HyperV_Helper.proacHoldingVM] = self.holdingVM
                    self.initialized = True
                    print ("holding vm: ", repr(self.holdingVM))
                    return True
            print("No holdingVM found")
            return False
        finally:
            if socketServer != None:
                socketServer.clean()

    #To get mounted NIC's mac address
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
            res.append(mac.replace("-", ":")) #win32 mixes up : & -...
        return res

#Utility class for HyperV_Runtime
class HyperV_Helper_NonWindows(HyperV_Helper) :

    def __init__(self,url,user,pwd):
        HyperV_Helper.__init__(self)
        self.url = url
        self.user = user
        self.pwd = pwd
        self.initialized = False

    #Return True if compliant environment is found
    def _test(url, user, pwd):
        """Test wether the environment matches
        the requirements for a non windows platform"""
        if url != None and user != None and pwd != None:
            return True
        else:
            return False
    _test = staticmethod(_test)

#HyperV Helper implementation for Windows Platform
class HyperV_Helper_Windows(HyperV_Helper) :

    def __init__(self,url,user,pwd):
        HyperV_Helper.__init__(self)
        self.url = url
        self.user = user
        self.pwd = pwd
        self.initialized = False

    #Return True if compliant environment is found
    def _test(url, user, pwd):
        if sys.platform == 'win32':
            try:
                hkey = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE,"SOFTWARE\\Microsoft\\Virtual Machine\\Guest\\Parameters")
                value, type = winreg.QueryValueEx(hkey, "VirtualMachineName")
                if value != None and value != "" and url != None and user != None and pwd != None:
                    return True
                else:
                    return False
            except Exception:
                return False
        else:
            return False
    _test = staticmethod(_test)

    #Tries to determine in which virtual machine we are running
    def _fixHoldingVirtualMachine(self):
        """Fix the current holding virtual machine
        thanks to value set as a hkey in windows registry"""
        hkey = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE,"SOFTWARE\\Microsoft\\Virtual Machine\\Guest\\Parameters")
        value, type = winreg.QueryValueEx(hkey, "VirtualMachineName")
        if value != None and value != "":
            self.holdingVM = value
            self.initialized = True
            return True
        else:
            return False

    #Used to retrieve a data set in virtual machine configuration
    def getData(self,key):
        """To get a data saved thanks to VirtualMachine2#pushData method
        belonging to ProActive Virtual Machine Management Project. One can
        also use native HyperV WMI api to push data. see
        Msvm_VirtualSystemManagementService#ModifyKvpItems method"""
        hkey = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE,"SOFTWARE\\Microsoft\\Virtual Machine\\External")
        try:
            value, type = winreg.QueryValueEx(hkey, key)
            return value
        except Exception:
            return None

class ProActiveSocketServer:

    def __init__(self,nbClients):
        varI = 0
        while varI <= 10: #circuit broker
            try:
                socket.setdefaulttimeout(HyperV_Helper.utilsSocketTimeout)
                self.ss = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.ss.bind(("127.0.0.1",0))
                self.ss.listen(nbClients)
                break
            except Exception:
                varI = varI + 1
                print("An error occurred while creating socket server for hyperv data exchange")
                traceback.print_exc()
        print("new ProActiveSocketServer for hyperv data exchange created. Listening on port " + repr(self.ss.getsockname()[1]))

    def processChildComm(self):
        print("PASocketServer is waiting for a connection for hyperv data exchange")
        result = []
        varJ = 0
        while varJ <= 5: #circuit broker
            try:
                (cs, address) = self.ss.accept()
                varI = 0
                while varI <= 10: #circuit broker
                    try:
                        received = cs.recv(PA_MESSAGE_LENGTH).decode("utf-8").strip().rstrip()
                        if received.find(EOF_MARKER) != -1:
                            break
                        if len(received) == 0:
                            print("A problem occurred while listening hyperv data exchange")
                            break
                        print("received data: " + received)
                        result.append(received)
                    except Exception:
                        print("An error occurred while listening hyperv data exchange")
                        traceback.print_exc()
                        varI = varI + 1
                cs.close()
                return result
            except Exception:
                print("An error occurred while accepting connection hyperv data exchange")
                traceback.print_exc()
                varJ = varJ + 1
        return result


    def getPort(self):
        return self.ss.getsockname()[1]

    def clean(self):
        self.ss.close()
