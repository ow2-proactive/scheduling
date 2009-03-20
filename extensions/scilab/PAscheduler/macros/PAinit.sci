function [] = PAinit()

     global PA_initialized

     version = ver();
     if strtod(part(version(1,2),1)) < 5
         error('This toolkit cannot be run on a version of Scilab anterior to version 5');
     end

     try
         schedulerdir = getenv('SCHEDULER_HOME');
     catch
         error('The environment variable SCHEDULER_HOME must be defined, use setenv to define it in Scilab');
     end

     if ~isdir(schedulerdir)
         error(strcat(['The path "',schedulerdir,'" doesn't exist or is not a directory']));
     end
     schedjar = fullfile(schedulerdir,'dist','lib','ProActive_Scheduler-core.jar');
     if length(fileinfo(schedjar)) == 0
         error(strcat(['Can''t locate the scheduler jar at ""';schedjar;'"" , please make sure that SCHEDULER_HOME refers to the correct directory.']));
     end

     sep=pathsep();

     // Add ProActive Scheduler to the scilab classpath
     initcp = javaclasspath();

     cp = [fullfile(schedulerdir,'dist','lib','ProActive_ResourceManager.jar'); initcp];
     cp = [fullfile(schedulerdir,'dist','lib','ProActive_Scheduler-core.jar'); cp];
     cp = [fullfile(schedulerdir,'dist','lib','ProActive_SRM-common.jar'); cp];
     cp = [fullfile(schedulerdir,'dist','lib','ProActive.jar'); cp];
     cp = [fullfile(schedulerdir,'dist','lib','javasci.jar');cp];
     javaclasspath(cp);

     // Call the native JNI connection to the Scheduler classes
     initEmbedded();

     PA_initialized = 1;
     predef(1);

     endfunction
