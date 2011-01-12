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


# user-data contains the command to launch built on the rm side
data = urllib2.urlopen("http://169.254.169.254/1.0/user-data").read()

# passing through the NAT requires knowing the public IP
ip = urllib2.urlopen("http://169.254.169.254/2009-04-04/" +
                     "meta-data/public-ipv4").read()

print data +\
    " -Dproactive.hostname=" + ip


