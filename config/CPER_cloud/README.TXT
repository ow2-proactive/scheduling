This folder contains configuration files that need to be overriden in order to use this Scheduler distribution on the CPER cloud computational cluster.

After you have got the Scheduler distribution, just issue this command
cp -rv --parents ./* ../../
from the current directory.

Afterwards, export the RCP and WebStart plugins and redeploy everything on the cluster.

Logging
~~~~~~~
By default, logging is done in the following fashion:
* all logs will go into the .logs directory of the Scheduler distribution
* the router outputs nothing at the console, and logs everything in router.log The log mode is INFO for performance reasons
* the resource manager will log:
	- all messages coming from its code to RM.log
	- all messages coming from the ProActive library to ProActive_RM.log
The loglevel is DEBUG by default; if the logging overhead poses a performance issue, you might wish to set it to INFO
* the scheduler will log its messages according just like it would do for any other distribution, but 
the messages coming from the ProActive library will go to ProActive_Scheduler.log
* the deployed nodes will log all the messages in the file nodes.log 

Check out ProActive and log4j documentation if you want to customize this behaviour.
