function [] = PAconnect(uri,credpath)

    global ('PA_initialized', 'PA_connected','PA_solver')
    initJavaStack();
    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
        jimport java.lang.System;
        jimport org.scilab.modules.gui.utils.ScilabPrintStream;
        if jinvoke(ScilabPrintStream,'isAvailable') then
            inst = jinvoke(ScilabPrintStream,'getInstance');
            addJavaObj(inst);
            jinvoke(System, 'setOut',inst);
            jinvoke(ScilabPrintStream,'setRedirect',[]);            
        end
    end
    opt=PAoptions();
    

    if type(PA_solver) ~= 1 then
        PA_connected = jinvoke(PA_solver,'isConnected');
    end
        
    if ~exists('PA_connected') | PA_connected ~= 1 
        deployJVM(opt);   
        ok = jinvoke(PA_solver,'join', uri);
        if ~ok then
            error('PAConnect::Error wile connecting');
        end
        dataspaces(opt);
        PA_connected = 1;
        disp(strcat(['Connection successful to ', uri]));
           
    end    
    if ~jinvoke(PA_solver,'isLoggedIn')  
        if argn(2) == 2 then
            login(credpath); 
        else
            login(); 
        end
                             
    else
        disp('Already connected');
    end    
    //jremove(ScilabSolver);
    clearJavaStack();
endfunction

function deployJVM(opt)
    global ('PA_scheduler_dir','PA_solver', 'PA_dsregistry', 'PA_jvminterface')
    jimport org.ow2.proactive.scheduler.ext.matsci.client.embedded.util.StandardJVMSpawnHelper;
    jimport java.lang.String;    
    deployer = jinvoke(StandardJVMSpawnHelper,'getInstance');
    addJavaObj(deployer);
    home = getenv('JAVA_HOME');
    fs=filesep();
    if length(home) > 0
        deployer.setJavaPath(home + fs + 'bin'+ fs +'java');
    end

    scheduling_dir = PA_scheduler_dir;

    dist_lib_dir = scheduling_dir + fs + 'dist' + fs + 'lib';
    if ~isdir(dist_lib_dir)
        plugins_dir = scheduling_dir +fs +'plugins';
        dirdir=dir(plugins_dir+fs+ 'org.ow2.proactive.scheduler.lib_*');
        dd=dirdir.name;
        dist_lib_dir = plugins_dir + fs + dd + fs +'lib';
        if ~isdir(dist_lib_dir)
            clearJavaStack();
            error('PAconnect::cannot find directory ' +dist_lib_dir);
        end
    end
    jars = opt.ProActiveJars;
    jarsjava = jarray('java.lang.String', size(jars,1));
    addJavaObj(jarsjava);
    for i=1:size(jars,1)      
        jartmp = jnewInstance(String, dist_lib_dir + fs + jars(i).entries);
        jarsjava(i-1) = jartmp;
        addJavaObj(jartmp);
    end
    jinvoke(deployer,'setDebug',opt.Debug);
    jinvoke(deployer,'setClasspathEntries',jarsjava);
    jinvoke(deployer,'setProActiveConfiguration',opt.ProActiveConfiguration);
    jinvoke(deployer,'setLog4JFile',opt.Log4JConfiguration);
    jinvoke(deployer,'setPolicyFile',opt.SecurityFile);
    jinvoke(deployer,'setClassName','org.ow2.proactive.scheduler.ext.matsci.middleman.MiddlemanDeployer');


    rmiport = opt.RmiPort;

    jinvoke(deployer,'setRmiPort',rmiport);

    pair = jinvoke(deployer,'deployOrLookup');
    itfs = jinvoke(pair,'getX');
    port = jinvoke(pair,'getY');
    PAoptions('RmiPort',port);    
    PA_solver = jinvoke(deployer,'getScilabEnvironment');    

    PA_dsregistry = jinvoke(deployer,'getRegistry');    

    PA_jvminterface = jinvoke(deployer,'getJvmInterface');
        
    
    disp('Connection to JVM successful');    
endfunction

function login(credpath)
    global ('PA_solver')
    jimport org.ow2.proactive.scheduler.ext.matsci.client.embedded.util.StandardJVMSpawnHelper;
    // Logging in
    if ~jinvoke(PA_solver,'isLoggedIn') then
        disp('Please enter login/password');
        if argn(2) == 1
            try
                jinvoke(PA_solver,'login',credpath);
            catch        
                clearJavaStack();
                error('PAconnect::Authentication error');
            end
        else
            deployer = jinvoke(StandardJVMSpawnHelper,'getInstance');
            addJavaObj(deployer);
            jinvoke(deployer,'startLoginGUI');
            while ~jinvoke(PA_solver,'isLoggedIn') & jinvoke(deployer,'getNbAttempts') <= 3
                xpause(1000*100);
            end
            if jinvoke(deployer,'getNbAttempts')  > 3 then
                clearJavaStack();
                error('PAconnect::Authentication error');
            end
        end
        disp('Login succesful');        
    end   

endfunction

function dataspaces(opt)
    global ('PA_dsregistry')    
    jinvoke(PA_dsregistry, 'init','ScilabInputSpace', 'ScilabOutputSpace', opt.Debug);
endfunction

function initJavaStack()
    global('JAVA_STACK')
    JAVA_STACK=list();    
endfunction

function addJavaObj(obj)
    global('JAVA_STACK')   
    JAVA_STACK($+1)=obj;    
endfunction

function clearJavaStack()
    global('JAVA_STACK')
    for i=length(JAVA_STACK):-1:1
        try
            jremove(JAVA_STACK(i));
        catch
        end
    end
    clearglobal('JAVA_STACK');
endfunction
