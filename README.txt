Thanks for your interest in ProActive Scheduling.

ProActive Scheduling {version}

You can find the documentation of ProActive Scheduler in the docs directory.

Javadoc and updated documentation are available online: http://proactive.inria.fr



*** Quick start :

* Set JAVA_HOME environment variable to the directory where 1.5 or greater JDK

* Start a command shell and go into the bin/[os] directory
into your installed scheduler home path. Then launch :
A database is used to store ProActive Scheduler
activities and to offer fault tolerance. The database is configured in the top of 
the 'hibernate.cfg.xml' file in the 'config/database/hibernate' directory.

* Next, you can start the scheduler by launching the startScheduler.[sh|bat]
script. If run without an argument, it will first start a Resources Manager on
the local host and deploy 4 nodes. Then the scheduler will be started and
connected to this resource Manager. Scheduler is starting sequence, that  is finished
when the line :
	 Scheduler successfully created on rmi://hostname:port/

is displayed. At this point, ProActive Scheduler is started with 4 nodes available.

A database is used to store ProActive Scheduler activities and to offer fault tolerance.
The database is configured in the 'hibernate.cfg.xml' file in the
'config/database/hibernate' directory.

* You can now submit a job. To do so, you can use the command-line controller.
Just start the userScheduler.[sh|bat] script with proper parameters:
~> userScheduler.[sh|bat] --submit ../../samples/jobs_descriptors/Job_8_tasks.xml
You will requested for login and password, and then the Job_PI.xml is submitted to the
scheduler. If you need a login and password, a default couple one is demo:demo.
There are many other jobs examples in job_descriptors directory.

* The scheduler is now scheduling this job. For further information, please refers to the
Scheduler documentation; an administration guide will help you to tune the scheduler,
and user guide will explain how to build and submit jobs.


*** Compilation :

If you want to recompile all sources and generate all jar files:

	o Under Linux:
	  cd compile
	  ./build deploy.all  (check that the build script has executable permission)

	o Under Windows:
	  cd compile
	  build.bat deploy.all

If you want only to compile all sources (and not the jar files):

	o Under Linux:
	  cd compile
	  ./build compile.all  (check that the build script has executable permission)

	o Under Windows:
	  cd compile
	  build.bat compile.all

If you have any problems or questions when using ProActive Scheduling, feel free to contact us
at proactive@ow2.org


*** Known bugs and issues:

Details can be found on the ProActive Jira bug-tracking system
(https://galpage-exp.inria.fr:8181/jira) :
- SCHEDULING-166 ProActive Scheduling 1.0.0 supports only 'one node per JVM' configuration (i.e.
GCMD vmCapacity set to 1).
- SCHEDULING-256 Concurrent access to the same job logs can lead to log loss.
- SCHEDULING-266 GCMD/Static empty node sources redeploy all nodes when some nodes are added.
- SCHEDULING-267 Scheduler and RM can be impacted by network latency.
- SCHEDULING-127 MacOSX and JSR223 :
Some features of ProActive Scheduling rely on Java Scripting API (JSR 223),
which is bugged under MacOSX/Java 1.5.0_16: some JSR 223 specific classes
contained in the AppleScriptEngine.jar (loaded from the boot classpath) are
compiled under Java 1.6. As a consequence, using default Java 1.5.0_16 for
starting any part of ProActive Scheduling (including graphical clients and
worker nodes) can lead to the following exception (if scripts capabilities
are used):
java.lang.UnsupportedClassVersionError: Bad version number in .classfile
To fix this issue, you must remove or rename the AppleScriptEngine.jar
from /System/Library/Java/Extensions directory.


*** Enjoy ProActive Scheduling !
