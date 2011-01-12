Thanks for your interest in ProActive Resource Manager {version}.

You can find the complete version of documentation in the docs directory or on
our website http://proactive.inria.fr.

*** Quick start :

* Set JAVA_HOME environment variable to the directory where java 1.5 or
greater is located.

* Start a command shell and go into the bin/[os] directory of your installed
resource manager home path. You can start it by launching the
rm-start[.bat] script. If run without argument, it starts an empty Resource
Manager(RM) on the local host. By specifying "-ln" parameter it will deploy 4 local nodes.

Once th RM is started the following line will be displayed
	Resource Manager successfully created on rmi://jily.inria.fr:1099/

In order to check the state of RM, number of nodes available and others or
to administrate the RM (deploy/remove nodes etc) use rm-client[.bat] script.
After providing a login information you will have an access to javascript
console which provides different means to manager the RM. Just type "help()"
there. As an alternative you can specify some parameters to rm-admin[.bat]
script (run "rm-client -h" for details). For example to list all nodes in the
RM just run
~> rm-client -ln

* For further information, please refers to the Resource Manager
* documentation.


*** Compilation :

If you want to recompile all sources and generate all jar files:

	o Under Linux:
	  cd compile
	  ./build deploy.rm  (check that the build script has executable permission)

	o Under Windows:
	  cd compile
	  build.bat deploy.rm

If you want only to compile all sources (and not the jar files):

	o Under Linux:
	  cd compile
	  ./build compile.rm  (check that the build script has executable permission)

	o Under Windows:
	  cd compile
	  build.bat compile.rm

If you have any problems or questions when using ProActive Resource Manager,
feel free to contact us at proactive@ow2.org


*** Known bugs and issues:

Details can be found on the ProActive Jira bug-tracking system
(http://bugs.activeeon.com):

*** Enjoy The ProActive Resource Manager
