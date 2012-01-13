Thanks for your interest in ProActive Scheduling.

ProActive Scheduling {version}

You can find the documentation of ProActive Scheduler in the docs directory.

Javadoc and updated documentation are available online: http://proactive.inria.fr



*** Quick start :

* Set JAVA_HOME environment variable to the directory where java 1.6 or
greater is located (if you need a java 1.5 version, please contact
ActiveEon at contact@activeeon.com).

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
~> scheduler-client[.bat] --submit ../../samples/jobs_descriptors/Job_PI.xml

You will be requested for login and password, and then the Job_PI.xml is
submitted to the scheduler (use for instance demo:demo). You may find many 
other jobs examples in samples/job_descriptors directory.

* For further information, please refers to the Scheduler documentation; the
administration guide will help you to tune the scheduler, and the user guide 
will explain how to build and submit jobs.


*** Prepare project for compilation

Scheduling project depends on the ProActive Programming project. To build Scheduling
it is necessary to copy there all binaries produced as result of Programming 
compilation: content of the 'Programming/dist/lib' into the 'Scheduler/lib/ProActive'.
This can be done using special ant target (this target assumes that special build property 
'programming.project.dir' contains path to the compiled ProActive Programming project):
    o Under Linux:
      cd compile
      ./build copy.dependencies  (check that the build script has executable permission)

    o Under Windows:
      cd compile
      build.bat copy.dependencies

Also special ant script was created for quick start with Scheduling project:
- check out 'build' project (svn://scm.gforge.inria.fr/svn/proactive/build)
- go to the build project
- execute 'ant prepare-scheduling' 
This ant target will check out ProActive Programming and Scheduling projects, 
compile Programming and will copy all required dependencies into the Scheduling, 
after this Scheduling project is completely ready for compilation (note: before executing 
ant script it is possible to modify some script parameters like svn url to use, 
see build/build.properties for all available options).
  

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
(http://bugs.activeeon.com/):

*** Enjoy ProActive Scheduling !
