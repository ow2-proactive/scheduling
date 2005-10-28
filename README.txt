Thanks for your interest in ProActive.

ProActive 3.0

You can find the documentation of ProActive in the docs directory.
For the Reference Card:
	docs/api/org/objectweb/proactive/doc-files/ReferenceCard.html
For API documentation:
	docs/api/index.html
For HTML manual:
	docs/api/org/objectweb/proactive/doc-files/index.html

For PDF formats visit:
	http://www-sop.inria.fr/oasis/ProActive/doc/index.html

For online version of the manual visit:
	http://www-sop.inria.fr/oasis/ProActive/doc/api/org/objectweb/proactive/doc-files/index.html
  

In order to start experimenting with ProActive 
	- set JAVA_HOME environment variable to the directory where JDK is installed
	- run the examples by going in the scripts directory (scripts/<platform>) and launching
	the suitable scripts for your platform.
	For instance:
	Under Linux:
		export JAVA_HOME=<JDK_INSTALL_PATH> (Bash syntax)
		cd scripts/unix
		./c3d_one_user.sh
		If you get a "permission denied" when running scripts, check the permissions of the scripts and change them accordingly.
		chmod -R 755 .
		Check also that "." in in your PATH, otherwise you can add it with "export PATH=.:$PATH" (Bash syntax)

	Under Windows
		set JAVA_HOME=<JDK_INSTALL_PATH>
		cd scripts\windows
		c3d_one_user.bat


It is also possible to modify the examples by editing the code located in src/org/objectweb/proactive/examples.
To recompile an example that you have modified, use the scripts located under the compile directory:
Under Linux
	cd compile
	./build examples  (check that the build script has executable permission)
Under Windows
	cd compile
	build.bat examples 


If you want to compile all sources:
Under Linux
	cd compile
	./build all  (check that the build script has executable permission)
Under Windows
	cd compile
	build.bat all 


If you have any problems or questions when using ProActive feel free to contact us at proactive@objectweb.org
Enjoy ProActive !

