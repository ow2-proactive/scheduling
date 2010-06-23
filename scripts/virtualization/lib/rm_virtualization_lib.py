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

import os
import re
import sys
import atexit
import traceback
from subprocess import *
import socket

urlopen = None
if sys.version_info[0] == 3 :
        import urllib.request
        urlopen = urllib.request.urlopen
else :
        import urllib
        urlopen = urllib.urlopen

#The name of the java program in charge of running RM Nodes and passing back the nodes' url
NODE_STARTER = "org.ow2.proactive.resourcemanager.utils.VirtualInfrastructureNodeStarter"
#Java program responsible for the creation of RM Nodes and their registration to the RM Core.
NODE_STARTER_REGISTER = "org.ow2.proactive.resourcemanager.utils.VirtualInfrastructureNodeStarterRegister"
#A watch dog to end communication when passing nodes' url and the message's length
EOF_MARKER = "EOF"
PA_MESSAGE_LENGTH = 1000
#The nodes' classpath
NODE_CLASSPATH = ["script-js.jar","jruby-engine.jar","jython-engine.jar","commons-logging-1.1.1.jar","ProActive.jar",\
                  "ProActive_SRM-common.jar","ProActive_ResourceManager.jar", "commons-httpclient-3.1.jar", \
                  "ProActive_Scheduler-worker.jar","commons-codec-1.3.jar", \
                  "js.jar", "jruby.jar", "jython.jar"]

#This file gives an implementation of base class for Resource Manager node starter.
#Each implementation is "Virtualization Software dependent".
class Abstract_Runtime :

    __socketServer = None
    SOCKET_TIMEOUT = 20
    __providers = [] #to keep known implementations
    __runtimes = [] #to keep known runtimes, to be able to ask for termination

    def __buildClassPath() :
        """builds the application class path"""
        res = ""
        cwd = os.path.dirname(sys.argv[0])
        libHome = cwd[:cwd.rfind("scripts" + os.path.sep)] +"lib" + os.path.sep
        for jar in NODE_CLASSPATH :
            res = res + libHome + jar + os.pathsep
        res= res + "."
        return res
    __buildClassPath = staticmethod(__buildClassPath)

    def _writeNodeURL(self,url,nodeUrlID):
        """To assign a key thanks to the hypervisor provider
        communication API with the id nodeUrlID and the value
        url"""
        raise NotImplementedError(caller + ' must be implemented in subclass')

    def _buildCommandForStarterRegister(self,rmUrl,creds,nodesourceName,holdingVM,hostCapacity,propList):
        """build the command and start the node starter register"""
        classpath = Abstract_Runtime.__buildClassPath()
        procs = []
        for i in [ i for i in range(hostCapacity+1)][1:] :
            command = [ "java" ]
            for prop in propList:
                command.append(prop)
            command.append( "-classpath" )
            command.append(classpath)
            command.append(NODE_STARTER_REGISTER)
            command.append(creds)
            command.append(rmUrl)
            command.append(nodesourceName)
            command.append(holdingVM)
            command.append(repr(i))
            print("command" + repr(i) + ": " + repr(command))
            proc = Popen(args = command, stdout = PIPE, stderr = PIPE)
            Abstract_Runtime.__registerRuntime(proc)
            procs.append(proc)
        return procs

    def _buildCommandForStarter(self,holdingVM,propList,hostCapacity):
        """build the command and start the node starter"""
        classpath = Abstract_Runtime.__buildClassPath()
        procs = []
        for i in range(hostCapacity) :
            command = [ "java" ]
            for prop in propList:
                command.append(prop)
            command.append( "-classpath" )
            command.append(classpath)
            command.append(NODE_STARTER)
            command.append(repr(Abstract_Runtime.__socketServer.getPort()))
            command.append(holdingVM)
            command.append(repr(i+1))
            print("command" + repr(i) + ": " + repr(command))
            proc = Popen(args = command, stdout = PIPE, stderr = PIPE)
            Abstract_Runtime.__registerRuntime(proc)
            procs.append(proc)
        return procs

    def _start(self, rmUrl, creds, nodesourceName, holdingVM, hostCapacity, propList) :
        """launch resource manager node.
        rmUrl: resource manager's url -> string
        rmUser: the user that will connect to the rm -> string
        rmPwd: the user's password ->string
        nodesourceName: the nodesource's name the node will be attached to -> string
        nodeName: the node's name -> string
        propList: dynamic properties set by user -> list"""
        procs = []
        if rmUrl is not None and rmUrl != "":
            procs = self._buildCommandForStarterRegister(rmUrl,creds,nodesourceName,holdingVM,hostCapacity,propList)
        else:
            Abstract_Runtime.__socketServer = ProActiveSocketServer(hostCapacity)
            procs = self._buildCommandForStarter(holdingVM,propList,hostCapacity)
        offset = 0
        if rmUrl is None or rmUrl == "":
            for proc in procs:
                urls = Abstract_Runtime.__socketServer.processChildComm()
                for varI, url in enumerate(urls):
                    self._writeNodeURL(url,offset + varI )
                    offset = offset + len(urls)
            Abstract_Runtime.__socketServer.clean()
        print("Node acquisition ended, now waiting for child process to exit...")
        for child in procs:
            child.wait()

    def __cleanChild():
        """method in charge of destroying every RM Nodes started"""
        for i in range(len(Abstract_Runtime.__runtimes)):
            try:
                proc = Abstract_Runtime.__runtimes[i]
                proc.terminate()
            except AttributeError:
                print ("An error occurred while cleaning environment.")
                print ("If you want to use this feature, be sure to run at least python 2.6")
        Abstract_Runtime.__socketServer.clean()
    __cleanChild = staticmethod(__cleanChild)

    def addProvider(provider):
        """to precise that a new implementation of Abstract_Runtime is available"""
        Abstract_Runtime.__providers.append(provider)
        print("provider " + repr(provider) + " added.")
    addProvider = staticmethod(addProvider)

    def __registerRuntime(rt):
        """To add a new RM Node that will be managed by this API"""
        Abstract_Runtime.__runtimes.append(rt)
        atexit.register(Abstract_Runtime.__cleanChild)
        print("new Runtime registered.")
    __registerRuntime = staticmethod(__registerRuntime)

    def getInstance():
        """this static method tries to initialize the environment
        returning the goot proactive provider. If you write new
        providers, add yours here"""
        for i in range(len(Abstract_Runtime.__providers)):
            x = Abstract_Runtime.__providers[i]
            res = x.isOk()
            if res != None:
                print ("find a matching environment")
                return x
        print ("no matching environment found")
        return None
    getInstance = staticmethod(getInstance)

#A Socket implementation for IPC between this python program
#and RM Node starter. This socket class is used to communicate
#the new created nodes' url.
class ProActiveSocketServer:

    def __init__(self,nbClients):
        varI = 0
        while varI <= 10: #circuit broker
            try:
                socket.setdefaulttimeout(Abstract_Runtime.SOCKET_TIMEOUT)
                self.ss = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                self.ss.bind(("127.0.0.1",0))
                self.ss.listen(nbClients)
                break
            except Exception:
                varI = varI + 1
                print("An error occurred while creating socket server")
                traceback.print_exc()
        print("new ProActiveSocketServer for node creation created. Listening on port " + repr(self.ss.getsockname()[1]))

    def processChildComm(self):
        print("PASocketServer is waiting for a connection for node creation on port ", self.getPort())
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
                            print("A problem occurred while listening for node creation")
                            break
                        print("received a new node creation: " + received)
                        result.append(received)
                    except Exception:
                        varI = varI + 1
                        print("An error occurred while processing communication for node creation")
                        traceback.print_exc()
                cs.close()
                return result
            except Exception:
                varJ = varJ + 1
                print("An error occurred while accepting communication for node creation")
                traceback.print_exc()
        return result

    def getPort(self):
        return self.ss.getsockname()[1]

    def clean(self):
        self.ss.close()
