Thanks for your interest in ProActive.

ProActive $ID:2007-08-30$

You can find the documentation of ProActive in the docs directory:

    * Offline Documentation in PDF format

    * Online Manual

    * Online Public API documentation

    * Online Complete API documentation


In order to start experimenting with ProActive:

    * Set JAVA_HOME environment variable to the directory where 1.5 or greater JDK is installed

    *

      Run the examples by going in the scripts directory (scripts/<platform>) and launching the suitable scripts for your platform.

      For instance:
          o

            Under Linux:
            export JAVA_HOME=<JDK_INSTALL_PATH> (Bash syntax)
            cd scripts/unix
            ./c3d_one_user.sh
            If you get a "permission denied" when running scripts, check the permissions of the scripts and change them accordingly.
            chmod -R 755 .
            Check also that "." in in your PATH, otherwise you can add it with "export PATH=.:$PATH" (Bash syntax)
          o

            Under Windows:
            set JAVA_HOME=<JDK_INSTALL_PATH>
            cd scripts\windows
            c3d_one_user.bat


It is also possible to modify the examples by editing the code located in src/org/objectweb/proactive/examples.

    *

      Under Linux:
      cd compile
      ./build examples  (check that the build script has executable permission)
    *

      Under Windows:
      cd compile
      build.bat examples


If you want to compile all sources and generate all jar files (might be useful with SVN version):

    *

      Under Linux:
      cd compile
      ./build deploy  (check that the build script has executable permission)
    *

      Under Windows:
      cd compile
      build.bat deploy


If you want only to compile all sources (and not the jar files):

    *

      Under Linux:
      cd compile
      ./build compile  (check that the build script has executable permission)
    *

      Under Windows:
      cd compile
      build.bat compile


If you have any problems or questions when using ProActive feel free to contact us at proactive@objectweb.org
Enjoy ProActive !
