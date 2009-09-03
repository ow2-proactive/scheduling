#!/usr/bin/python

########################################################################### 80 #
#
# params.py
#
# reads instance user-data and meta-data to build parts
# of the Java command required to run the node.
# the other part is environment specific and can hardly be guessed from here
#

import urllib2
import re
import random


# user-data contains mandatory info for node startup
data = urllib2.urlopen("http://169.254.169.254/1.0/user-data").read()

# passing through the NAT requires knowing the public IP
ip = urllib2.urlopen("http://169.254.169.254/2009-04-04/" +
                     "meta-data/public-ipv4").read()

# extract the protocol and port from the user-data
pat = re.compile(r'[\s]*rmUrl[\s]*=[\s]*(.+)[\s]*')
res =  pat.findall(data)
if len(res) != 1:
    raise NameError("Could not extract RM Url from user-data.")
rmUrl = res[0]

# node should expose itself through HTTP because of the network architecture
proto = "http"
port_prop = "proactive.http.port"

# node HTTP port
pat = re.compile(r'[\s]*nodePort[\s]*=[\s]*(.+)[\s]*')
res =  pat.findall(data)
if len(res) != 1:
    # no HTTP port specified, trying 80
    port = 80
port = res[0]

# RM login
pat = re.compile(r'[\s]*rmLogin[\s]*=[\s]*(.+)[\s]*')
res =  pat.findall(data)
if len(res) != 1:
    raise NameError("Could not extract RM Login from user-data.")
rmLogin = res[0]

# RM pass
pat = re.compile(r'[\s]*rmPass[\s]*=[\s]*(.+)[\s]*')
res =  pat.findall(data)
if len(res) != 1:
    raise NameError("Could not extract RM Pass from user-data.")
rmPass = res[0]

# remote NS name
pat = re.compile(r'[\s]*nodeSource[\s]*=[\s]*(.+)[\s]*')
res =  pat.findall(data)
if len(res) != 1:
    raise NameError("Could not extract nodeSource from user-data.")
nodeSource = res[0]


# generate a pseudo random node name containing the instance type
alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
inst = urllib2.urlopen("http://169.254.169.254/2009-04-04/meta-data/" +
                       "instance-type").read().replace(".","-")

nodeName = "EC2-" + inst + "-"
for i in random.sample(alpha, 16):
    nodeName += i


# build the command, it only misses the classpath and -D*.home props
print "-Dproactive.communication.protocol=" + proto +" "+\
    "-D" + port_prop + "=" + port +" "+\
    "-Dproactive.hostname=" + ip +" "+\
    "org.ow2.proactive.resourcemanager.utils.PAAgentServiceRMStarter" +\
    " "+rmLogin+" "+rmPass+" "+rmUrl+" "+nodeName+" "+nodeSource

