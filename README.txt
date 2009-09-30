Thanks for your interest in ProActive Scheduling.

ProActive Scheduling {version}

You can find the documentation of ProActive Scheduler in the docs directory.

Javadoc and updated documentation are available online: http://proactive.inria.fr



*** Quick start :

* Set JAVA_HOME environment variable to the directory where java 1.5 or
greater is located.

* Start a command shell and go into the bin/[os] directory of your installed 
scheduler home path. You can start the scheduler by launching the 
scheduler-start[.bat] script. If ran without argument, it starts a Resource
Manager on the local host and deploy 4 nodes. Scheduler finishes its starting 
sequence when the following line is displayed:
	 Scheduler successfully created on rmi://hostname:port/
At this point, ProActive Scheduler is started with 4 free nodes.

A database is used to store ProActive Scheduler activities and to
offer fault tolerance.The database is configured in the
'hibernate.cfg.xml' file in the 'config/scheduler/database/hibernate' directory.

* You can now submit a job. To do so, use the command-line controller.
Just start the userScheduler.[sh|bat] script with proper parameters:
~> scheduler-user[.bat] --submit ../../samples/jobs_descriptors/Job_PI.xml

You will be requested for login and password, and then the Job_PI.xml is
submitted to the scheduler (use for instance demo:demo). You may find many 
other jobs examples in samples/job_descriptors directory.

* For further information, please refers to the Scheduler documentation; the
administration guide will help you to tune the scheduler, and the user guide 
will explain how to build and submit jobs.


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

If you have any problems or questions when using ProActive Scheduling,
feel free to contact us at proactive@ow2.org


*** Known bugs and issues:

Details can be found on the ProActive Jira bug-tracking system
(https://galpage-exp.inria.fr:8181/jira):

- SCHEDULING-166 ProActive Scheduling 1.0.0 supports only 'one node
  per JVM' configuration (i.e. GCMD vmCapacity set to 1).

- SCHEDULING-256 Concurrent access to the same job logs can lead to
  log loss.

- SCHEDULING-266 GCMD/Static empty node sources redeploy all nodes
  when some nodes are added.

- SCHEDULING-267 Scheduler and RM can be impacted by network latency.

- SCHEDULING-127 MacOSX and JSR223 :
Some features of ProActive Scheduling rely on Java Scripting API (JSR
223), which is bugged under MacOSX/Java 1.5.0_16: some JSR 223
specific classes contained in the AppleScriptEngine.jar (loaded from
the boot classpath) are compiled under Java 1.6. As a consequence,
using default Java 1.5.0_16 for starting any part of ProActive
Scheduling (including graphical clients and worker nodes) can lead to
the following exception (if scripts capabilities are used):
java.lang.UnsupportedClassVersionError: Bad version number in
.classfile To fix this issue, you must remove or rename the
AppleScriptEngine.jar from /System/Library/Java/Extensions directory.


*** Enjoy ProActive Scheduling !
