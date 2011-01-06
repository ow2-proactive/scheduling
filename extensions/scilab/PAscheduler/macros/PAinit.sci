function [] = PAinit()

    global PA_initialized

    version = ver();
    if strtod(part(version(1,2),1)) < 5
        error('This toolkit cannot be run on a version of Scilab anterior to version 5');
    end

    try
        scheduling_dir = getenv('SCHEDULER_HOME');
    catch
        error('The environment variable SCHEDULER_HOME must be defined, use setenv to define it in Scilab');
    end

    if ~isdir(scheduling_dir)
        error(strcat(['The path "',schedulerdir,'" doesn''t exist or is not a directory']));
    end

    schedjar = fullfile(scheduling_dir,'dist','lib','ProActive_Scheduler-client.jar');
    if length(fileinfo(schedjar)) == 0
        error(strcat(['Can''t locate the scheduler jar at ""';schedjar;'"" , please make sure that SCHEDULER_HOME refers to the correct directory.']));
    end
    opt=PAoptions();

    clzfile = class('java.io.File');
        
    // Log4J file
    log4jFile = newInstance(clzfile,strcat([scheduling_dir, filesep(), 'config', filesep(), 'log4j', filesep(), 'log4j-client']));
    log4jFileUri = invoke(log4jFile,'toURI');
    urlLog4jFile = invoke(log4jFileUri,'toURL');
    finalstring = invoke(urlLog4jFile,'toExternalForm');
    system_setproperty('log4j.configuration',unwrap(finalstring));
    system_setproperty('proactive.configuration', opt.ProActiveConfiguration);

    // Policy
    system_setproperty('java.security.policy',strcat([scheduling_dir, filesep(), 'config', filesep(), 'scheduler.java.policy']));

    // Dist libs
    dist_lib_dir = strcat([scheduling_dir, filesep(), 'dist', filesep(), 'lib']);

    sep=pathsep();

    // Add ProActive Scheduler to the scilab classpath
    initcp = javaclasspath();
    if strcmp(getos(),'Windows') == 0
        for i=1:size(initcp,1)
            initcp(i)=getlongpathname(strsubst(initcp(1),'%20',' '))
        end
    end

    listjars = opt.ProActiveJars;

    cp = initcp;
    
    for i=1:size(listjars,1)        
        cp = [cp; fullfile(dist_lib_dir,listjars(i).entries)];
    end

    javaclasspath(cp);
        

    // Call the native JNI connection to the Scheduler classes
    //initEmbedded();

    PA_initialized = 1;
    predef(1);

endfunction
