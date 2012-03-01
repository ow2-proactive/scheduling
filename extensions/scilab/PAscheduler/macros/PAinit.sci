function [] = PAinit()

    global ('PA_initialized', 'PA_scheduler_dir')

    jautoUnwrap(%t);    

    version = ver();
    if strtod(part(version(1,2),1)) < 5
        error('This toolkit cannot be run on a version of Scilab anterior to version 5');
    end
    if ~exists('PA_scheduler_dir') 
        error('The environment variable SCHEDULER_HOME must be defined, use setenv to define it in Scilab');        
    end
    // disp(PA_scheduler_dir)
    if ~isdir(PA_scheduler_dir)
        error(strcat(['The path "',schedulerdir,'" doesn''t exist or is not a directory']));
    end
    plugins_dir = fullfile(PA_scheduler_dir,'plugins');
    if isdir(plugins_dir) then
        release_dir = listfiles(fullfile(PA_scheduler_dir,'plugins','org.ow2.proactive.scheduler.lib_*'));
        dist_lib_dir = fullfile(release_dir,'lib')        
    else
        dist_lib_dir = fullfile(PA_scheduler_dir,'dist','lib');        
    end   
    schedjar=fullfile(dist_lib_dir,'ProActive_Scheduler-matsciemb.jar'); 
    if length(fileinfo(schedjar)) == 0 
        error(strcat(['Can''t locate the scheduler jar at ""';schedjar;'"" , please make sure that SCHEDULER_HOME refers to the correct directory.']));
    end    
    opt=PAoptions();

    jimport java.io.File;
            

    sep=pathsep();

    // Add ProActive Scheduler to the scilab classpath
    initcp = javaclasspath();
    if strcmp(getos(),'Windows') == 0
        for i=1:size(initcp,1)
            initcp(i)=getlongpathname(strsubst(initcp(1),'%20',' '))
        end
    end

    listjars = opt.EmbeddedJars;

    cp = initcp;
    
    for i=1:size(listjars,1)        
        cp = [cp; fullfile(dist_lib_dir,listjars(i).entries)];
    end

    javaclasspath(cp);
        

    // Call the native JNI connection to the Scheduler classes
    //initEmbedded();

    PA_initialized = 1;

endfunction
