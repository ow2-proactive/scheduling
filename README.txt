Thanks for your interest in ProActive Scheduling.

ProActive Scheduling 2008-09-08 14:04:11

IMPORTANT NOTE :
		
Some parts of the ProActive Scheduler and ProActive  Resource Manager rely on Java Scripting capabilities.
As a consequence, it requires either:
	- 1.6 or greater Java Runtime Environment, without any modifications,
	- or, with a 1.5 JRE, the JSR 223 jar files (available at :  http://jcp.org/en/jsr/detail?id=223)
		    * First, the script-api.jar, script-js.jar and js.jar files must be added 
			in the /dist/lib/ directory if you are using the bin release of
			ProActive Scheduler, or in the /ProActive/lib/ directory if you build ProActive Scheduler from the source release.
		    * Then the java5_jsr223_patch.jar patch (released with the Scheduler RCP Client)
			should be executed in the Scheduler RCP Client directory : unzip the
			java5_jsr223_patch.zip file and execute java -jar java5_jsr223_patch.jar.

You can find the documentation of ProActive Scheduler in the docs directory :

Javadoc and updated documentation are available online: http://proactive.inria.fr

Quick start :

* Set JAVA_HOME environment variable to the directory where 1.5 or greater JDK

* Start a command shell and go into the bin/[os] directory
into your installed scheduler home path. Then launch :
createDataBase.[sh|bat] ../../config/database/scheduler_db.cfg 
script to create the database. This database will be used to store ProActive Scheduler
activities and to offer fault tolerance. The file given as parameter -
scheduler_db.cfg - is the configuration file for the database.

* Next, we start the scheduler by launching the startScheduler.[sh|bat]
script. If run without an argument, it will first start a Resources Manager on
the local host and deploy 4 nodes. Then the scheduler will be started and
connected to this resource Manager. Scheduler is starting sequence, that  is finished
when the line :
	 [SCHEDULER] Scheduler has just been started !

is displayed. At this point, ProActive Scheduler is started with 4 nodes available.

* You now submit a job. To do so, just start the
submit.[sh|bat] script with proper parameters. You can try using :
submit.[sh|bat] -j ../../jobs_descriptors/Job_8_tasks.xml -n 1, this will
request for login and password, and then submit this job to the scheduler. If
you need a login and password, a default couple one is user1:pwd1
There are many other jobs examples in job_descriptors directory.


* Once executed, you can see that the scheduler is now scheduling this
job. For further information, please refers to the Scheduler
documentation; an administration guide will help you to tune your scheduler, 
and user guide will explain how to build jobs.


If you want to recompile all sources and generate all jar files (might be useful with SVN version):

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


If you have any problems or questions when using ProActive Scheduling, feel free to contact us at proactive@ow2.org

Enjoy ProActive Scheduling !
