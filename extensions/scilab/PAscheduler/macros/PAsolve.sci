function outputs = PAsolve(varargin)
    [locals,globals] = who_user1();
    ke=grep(locals,['varargin']);
    locals(ke)=[];
    global ('PA_connected','PA_solver', 'SOLVEid', 'PAResult_TasksDB')

    if ~exists('PA_connected') | PA_connected ~= 1
        error('A connection to the ProActive scheduler must be established in order to use PAsolve, see PAconnect');
    end

    if exists('SOLVEid') & type(SOLVEid) == 8
        SOLVEid = SOLVEid + 1;
    else
        SOLVEid = int32(1);
    end
    
    if typeof(PAResult_TasksDB) ~= 'list'
        PAResult_TasksDB = list();
    end
    
    opt=PAoptions();
    
    deff ("y=ischar(x)","y=type(x)==10","n");

    [Tasks, NN, MM]=parseParams(varargin(:));

    

    initJavaStack();

    jimport org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
    jimport org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
    jimport org.ow2.proactive.scheduler.ext.common.util.FileUtils;       
        
    //addJavaObj(PASolveScilabGlobalConfig);
    //addJavaObj(PASolveScilabTaskConfig); 
    //addJavaObj(FileUtils);
            

    solve_config = jnewInstance(PASolveScilabGlobalConfig);
    addJavaObj(solve_config);
    
    task_configs = jarray('org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig', NN,MM);
    addJavaObj(task_configs);
    for i=1:NN       
        for j=1:MM
            t_conf = jnewInstance(PASolveScilabTaskConfig);
            addJavaObj(t_conf);
            task_configs(i-1,j-1) = t_conf;
        end
    end
                

    initSolveConfig(solve_config, opt);

    [taskFilesToClean,pa_dir,curr_dir,fs,subdir] = initDirectories(opt,solve_config,NN,SOLVEid);

    initDS(solve_config,opt);              
    
    
    taskFilesToClean = initTransferSource(task_configs,solve_config,opt,Tasks, SOLVEid,taskFilesToClean, pa_dir,fs,NN,MM);
    
    initTransferEnv(locals,globals,solve_config,opt,SOLVEid,taskFilesToClean, pa_dir,fs);
    
    initInputFiles(task_configs,solve_config,opt,Tasks,NN,MM);
    
    initOutputFiles(task_configs,solve_config,opt,Tasks,NN,MM);
    
    [outVarFiles, inputscript, mainScript,taskFilesToClean] = initParameters(task_configs,solve_config,opt,Tasks,pa_dir,fs,NN,MM,taskFilesToClean);
    
    initOtherTCAttributes(NN,MM, task_configs, Tasks);
        
       
    //addJavaObj(ScilabSolver);
    //addJavaObj(PAFuture);
        
    jobinfo = PA_solver.solve(solve_config, task_configs);    
    addJavaObj(jobinfo);    
    jidjava = jinvoke(jobinfo,'getJobId');
    addJavaObj(jidjava);
    jid = string(jidjava);
    disp('Job submitted : '+ jid);    

    ftnjava = jinvoke(jobinfo,'getFinalTasksNamesAsList');
    ftn = list();
    
    addJavaObj(ftnjava);
    taskinfo = struct('cleanFileSet',[],'cleanDirSet',[], 'outFile',[], 'jobid',[], 'taskid',[] );
    results=list(NN);
    for i=1:NN
        tidjava = jinvoke(ftnjava,'get',i-1);
        addJavaObj(tidjava);
        ftn(i) = string(tidjava);
        taskinfo.cleanFileSet = taskFilesToClean(i);
        taskinfo.cleanDirSet = list(pa_dir);
        taskinfo.outFile = outVarFiles(i);
        taskinfo.jobid = jid;
        
        taskinfo.taskid = ftn(i);
        taskinfo.sid = SOLVEid;        

        results(i)=PAResult(taskinfo);
    end    
    PAResult_TasksDB(SOLVEid) = ftn;
    outputs = PAResL(results);
    clearJavaStack();

endfunction

// Parse command line parameters
function [Tasks, NN, MM]=parseParams(varargin)
    
    if ischar(varargin(1)) then
        Func = varargin(1);
        NN=length(varargin)-1; 
        Tasks = PATask(1,NN);       
        Tasks(1,1:NN).Func = Func;
        for i=1:NN      
            if typeof(varargin(i+1)) == 'list'      
                Tasks(1,i).Params =varargin(i+1);
            else
                Tasks(1,i).Params =list(varargin(i+1));
            end
        end
        MM = 1;
    elseif typeof(varargin(1)) == 'PATask'
        if length(varargin) == 1
            Tasks = varargin(1);
            NN = size(Tasks,2);
            MM = size(Tasks,1);
        else
            NN=argn(2);
            MM = -1;
            for i=1:NN
                if typeof(varargin(i)) == 'PATask'
                    if (size(varargin(i),2) ~= 1)
                        error(strcat(['parameter ', string(i), ' should be a column vector.']));
                    end
                    sz = size(varargin(i),1);
                    if MM == -1
                        MM = sz;
                        Tasks=PATask(MM,NN);
                    elseif MM ~= sz
                        error(strcat(['parameter ', string(i), ' should be a column vector of the same length than other parameters.']));
                    end
                    Tasks(1:sz,i) = varargin(i);
                else
                    error(strcat(['parameter ', string(i), ' is a ', typeof(varargin(i)), ', expected PATask instead.']));
                end
            end

        end
    end
endfunction

// Initialize used directories
function [taskFilesToClean,pa_dir,curr_dir,fs,subdir] = initDirectories(opt,solve_config,NN,solveid)
    jimport java.io.File;
    jimport java.lang.String;
    //addJavaObj(File);   
    curr_dir = pwd();
    fs=filesep();
    curr_dir_java = jnewInstance(File,curr_dir);
    addJavaObj(curr_dir_java);
    if ~jinvoke(curr_dir_java,'canWrite')
        clearJavaStack();       
        error('Current Directory should have write access rights');
    end    
    // PAScheduler sub directory init

    subdir = '.PAScheduler';    

    if isempty(opt.CustomDataspaceURL)
        if ~isdir(curr_dir + fs + subdir)
            mkdir(curr_dir,subdir);
        end
        if ~isdir(curr_dir + fs + subdir + fs + string(solveid))
            mkdir(curr_dir + fs + subdir,string(solveid));
        end
        pa_dir = curr_dir + fs + subdir + fs + string(solveid);
    else
        if isempty(opt.CustomDataspacePath)
            clearJavaStack();    
            error('if CustomDataspaceURL is specified, CustomDataspacePath must be specified also');
        end
        if ~isdir(opt.CustomDataspacePath + fs + subdir)
            mkdir(opt.CustomDataspacePath,subdir);
        end
        if ~isdir(opt.CustomDataspacePath + fs + subdir + fs + string(solveid))
            mkdir(opt.CustomDataspacePath + fs + subdir , string(solveid));
        end
        pa_dir = opt.CustomDataspacePath + fs + subdir + fs + string(solveid);
    end
       
    
    taskFilesToClean=list();
    for i=1:NN
        taskFilesToClean($+1)=list();
    end
    
    subDirNames = jarray('java.lang.String', 2);
    addJavaObj(subDirNames);
    strName1 = jnewInstance(String,subdir);
    addJavaObj(strName1);
    subDirNames(0) = strName1;
    strName2 = jnewInstance(String,string(solveid));
    addJavaObj(strName2);
    subDirNames(1) = strName2;
    jinvoke(solve_config,'setTempSubDirNames',subDirNames);
    
    subdir = subdir + '/' + string(solveid); 
endfunction

// Initialize Data Spaces
function initDS(solve_config,opt)
    global('PA_dsregistry')    
    //addJavaObj(AODataspaceRegistry); 
    if isempty(opt.CustomDataspaceURL)   
        try                     
            unreifiable = jinvoke(PA_dsregistry,'createDataSpace',curr_dir);
        catch 
            error('There was a problem contacting the middleman Java Virtual Machine, please reconnect using PAconnect');
        end
        addJavaObj(unreifiable);
        pair = jinvoke(unreifiable,'get');
        addJavaObj(pair);
        px=jinvoke(pair,'getX');
        py=jinvoke(pair,'getY');
        addJavaObj(px);
        addJavaObj(py);
        pxs = jcast(px,'java.lang.String');
        pys = jcast(py,'java.lang.String');
        addJavaObj(pxs);
        addJavaObj(pys);
        solve_config.setInputSpaceURL(pxs);
        solve_config.setOutputSpaceURL(pys);
        if opt.Debug then
            disp('using Dataspace:')
            disp(pxs)
        end

    else
        solve_config.setOutputSpaceURL(opt.CustomDataspaceURL);
        solve_config.setInputSpaceURL(opt.CustomDataspaceURL);
    end
endfunction

// Setting PAsolveConfig properties
function initSolveConfig(solve_config,opt)
    jimport java.net.URL;
    //addJavaObj(URL);
    solve_config.setJobName(opt.JobName);
    solve_config.setJobDescription(opt.JobDescription);
    solve_config.setDebug(opt.Debug);
    solve_config.setTransferEnv(opt.TransferEnv);
    solve_config.setFork(opt.Fork);
    solve_config.setRunAsMe(opt.RunAsMe);    
    solve_config.setPriority(opt.Priority);    
    //solve_config.setTransferEnv(opt.TransferEnv);       
    jinvoke(solve_config,'setWindowsStartupOptionsAsString',opt.WindowsStartupOptions);
    jinvoke(solve_config,'setLinuxStartupOptionsAsString',opt.LinuxStartupOptions);

    solve_config.setInputSpaceName('ScilabInputSpace');
    solve_config.setOutputSpaceName('ScilabOutputSpace');

    solve_config.setVersionPref(opt.VersionPref);
    solve_config.setVersionRejAsString(opt.VersionRej);
    solve_config.setVersionMin(opt.VersionMin);
    solve_config.setVersionMax(opt.VersionMax);
    solve_config.setCheckMatSciUrl(opt.FindScilabScript);
    if ischar(opt.CustomScript)
        selects = opt.CustomScript
        try
            url=jnewInstance(URL,selects);            
            ok = %t;
        catch 
            ok = %f;
        end
        jremove(url);

        if ~ok            
            solve_config.setCustomScriptUrl(strcat(['file:', selects]));
        else
            solve_config.setCustomScriptUrl(selects);
        end
        solve_config.setCustomScriptStatic(opt.CustomScriptStatic);
        solve_config.setCustomScriptParams(opt.CustomScriptParams);
    end

    solve_config.setZipSourceFiles(%f);
endfunction

// Initialize Task Config source files to be transferred
function taskFilesToClean = initTransferSource(task_configs,solve_config,opt,Tasks, solveid,taskFilesToClean, pa_dir,fs,NN,MM)    
    jimport java.lang.String;
    //addJavaObj(String);
    for i=1:NN       
        for j=1:MM
            t_conf = task_configs(i-1,j-1);
            // Function
            Func = Tasks(j,i).Func;
            execstr(strcat(['functype = type(';Func;');']));
            if (functype <> 13) & (functype <> 11)  then
                clearJavaStack();
                error('invalid function type for function ""' + Func + '"". Consider calling this function with a macro instead.');
            end

            // Source Files
            if ~isempty(Tasks(j,i).Sources) then
                srcs = Tasks(j,i).Sources;                                
                for k=1:length(srcs)
                    srcPath = srcs(k);
                    if isfile(srcPath) then
                        [ppath,fname,extension]=fileparts(srcPath);
                        srcName = strcat(strcat([fname,extension]));

                        if opt.Debug then
                            disp(strcat(['Copying file ', srcPath, ' to ',pa_dir]));
                        end
                        copyfile(srcPath,pa_dir);
                        // TODO find a cleaning mechanisme
                        //taskFilesToClean(i)=lstcat(taskFilesToClean(i), list(pa_dir+fs+fname+extension));
                        //tmpFiles($+1)=strcat([pa_dir,fs,fname,extension]);

                        strName = jnewInstance(String,srcName);
                        addJavaObj(strName);
                        t_conf.addSourceFile(strName);
                    else
                        clearJavaStack();
                        error(strcat(['Source file ', srcPath, ' cannot be found']));
                    end

                end
            end
            
            // Saving main function name (with or without Sources attribute)
            sourceNames = jarray('java.lang.String', 1);
            addJavaObj(sourceNames);
            sFN = 'ScilabPAsolve_src'+string(solveid)+indToFile([i j])+'.bin';
            if opt.Debug then
                disp('Saving function '+Func+' into file ' +pa_dir+fs+sFN);
            end
            execstr('save(pa_dir+fs+sFN,'+Func+');');
            strName = jnewInstance(String,sFN);
            addJavaObj(strName);
            sourceNames(0) = strName;
            t_conf.setFunctionVarFiles(sourceNames);                
            code=[];
            taskFilesToClean(i)=lstcat(taskFilesToClean(i), list(pa_dir+fs+sFN));


            t_conf.setFunctionName(Func);
            t_conf.addSourceFile(sFN);
        end
    end
endfunction

function initTransferEnv(locals,globals,solve_config,opt,solveid,taskFilesToClean, pa_dir,fs)
    // Transfering the environment    
    
    if opt.TransferEnv
        jimport java.lang.String;
        envMatName = 'ScilabPAsolveEnv_'+ string(solveid)+ '.mat';
        envFilePath = pa_dir + fs  + envMatName;
        bigstr='';
        for i=1:size(locals,1)
            bigstr=bigstr+','+locals(i);
        end 
        for i=1:size(globals,1)
            bigstr=bigstr+','+globals(i);
        end               
        execstr('save('''+envFilePath+''''+bigstr+')');
        if opt.Debug then
            disp('Saving Environment vars '+bigstr+' in '+envFilePath);
        end        
        if size(globals,1) > 0 then
            globalNames = jarray('java.lang.String', size(globals,1));
            addJavaObj(globalNames);
        else 
            globalNames = [];
        end
                
        for i=1:size(globals,1)
            name = jnewInstance(String,globals(i));
            addJavaObj(name);
        end
        jinvoke(solve_config,'setEnvMatFileName',envMatName);     
        jinvoke(solve_config,'setEnvGlobalNames',globalNames);         
    end
endfunction

// Initialize Task Config Input Files
function initInputFiles(task_configs,solve_config,opt,Tasks,NN,MM) 
    jimport java.lang.String;
    //addJavaObj(String);
    for i=1:NN       
        for j=1:MM
            t_conf = task_configs(i-1,j-1);
            // Input Files
            if ~isempty(Tasks(j,i).InputFiles) then
                ilen = length(Tasks(j,i).InputFiles);
                if ilen > 0 then
                    inputFiles = jarray('java.lang.String', ilen);
                    addJavaObj(inputFiles);
                    filelist = Tasks(j,i).InputFiles;
                    for k=1:ilen
                        filename = filelist(k);
                        ifstr = jnewInstance(String,strsubst(filename,'\','/'));
                        addJavaObj(ifstr);
                        inputFiles(k-1)=ifstr;
                    end

                    t_conf.setInputFiles(inputFiles);
                    t_conf.setInputFilesThere(%t);
                end                
            end
        end
    end
endfunction

// Initialize Task Config Ouput Files
function initOutputFiles(task_configs,solve_config,opt,Tasks,NN,MM) 
    jimport java.lang.String;
    //addJavaObj(String);
    for i=1:NN       
        for j=1:MM
            t_conf = task_configs(i-1,j-1);
            if ~isempty(Tasks(j,i).OutputFiles) then
                filelist = Tasks(j,i).OutputFiles;
                ilen = length(filelist);
                if ilen > 0 then
                    outputFiles = jarray('java.lang.String', ilen);
                    addJavaObj(outputFiles);
                    for k=1:ilen
                        filename = filelist(k);
                        ofstr = jnewInstance(String,strsubst(filename,'\','/'));
                        addJavaObj(ofstr);
                        outputFiles(k-1)=ofstr;
                    end

                    t_conf.setOutputFiles(outputFiles);
                    t_conf.setOutputFilesThere(%t);
                end
            end
        end
    end
endfunction

// Initialize Task Config Input Parameters
function [outVarFiles, inputscript, mainScript,taskFilesToClean] = initParameters(task_configs,solve_config,opt,Tasks,pa_dir,fs,NN,MM,taskFilesToClean) 
    
    variableInFileBaseName = ['ScilabPAsolveVarIn_' string(SOLVEid)];
    variableOutFileBaseName = ['ScilabPAsolveVarOut_' string(SOLVEid)];
    outVarFiles = list(NN);  
    
    for i=1:NN       
        for j=1:MM
            t_conf = task_configs(i-1,j-1);
            // Params
            argi = Tasks(j,i).Params;
            if opt.TransferVariables
                inVarFN = strcat([variableInFileBaseName, indToFile([i j]), '.dat']);
                outVarFN = strcat([variableOutFileBaseName, indToFile([i j]), '.dat']);
                inVarFP = strcat([pa_dir, fs, inVarFN]);
                outVarFP = strcat([pa_dir, fs, outVarFN]);
                // Creating input parameters mat files
                fd=mopen(inVarFP,'wb'); 
                inl = argi;
                if length(inl) == 0
                    inl=list(%t);
                end
                for k=1:length(inl)
                    execstr('in'+string(k)+'=inl(k);');
                    execstr('save(fd,in'+string(k)+')');
                end
                mclose(fd);

                jinvoke(t_conf,'setInputVariablesFileName',inVarFN);
                jinvoke(t_conf,'setOutputVariablesFileName',outVarFN);
                if j > 1 & Tasks(j,i).Compose
                    cinVarFN = strcat([variableOutFileBaseName,indToFile([i j-1]),'.dat']);
                    cinVarFP = pa_dir+fs+cinVarFN;                    
                    jinvoke(t_conf,'setComposedInputVariablesFileName',cinVarFN);                    
                end
                outVarFiles(i) = outVarFP;
                taskFilesToClean(i)=lstcat(taskFilesToClean(i), list(inVarFP));
                //if j < MM
                // because of disconnected mode, the final out is handled
                //differently
                taskFilesToClean(i)=lstcat(taskFilesToClean(i), list(outVarFP));
                //end

                inputscript = 'i=0';
            else
                inputscript = createInputScript(argi);
            end 
            
            t_conf.setInputScript(inputscript);

            //mainScript = createMainScript(Func, opt);
            mainScript = 'out = '+Tasks(j,i).Func+'(';
            if j > 1 & Tasks(j,i).Compose
                mainScript = mainScript + 'in';
                if length(argi) > 0 then
                    mainScript = mainScript + ',';
                end
            end
            if length(argi) > 0
                for k=1:length(argi)-1
                    mainScript = mainScript + 'in'+string(k)+',';
                end
                mainScript = mainScript + ('in'+string(length(argi)));
            end
            mainScript = mainScript + ');';
            t_conf.setMainScript(mainScript);

            t_conf.setOutputs('out');
        end
    end
endfunction

function initOtherTCAttributes(NN,MM, task_configs, Tasks)
    jimport java.net.URL;
    //addJavaObj(URL);
    for i=1:NN       
        for j=1:MM
            t_conf = task_configs(i-1,j-1);
            if ~isempty(Tasks(j,i).Description) then
                jinvoke(t_conf,'setDescription',Tasks(j,i).Description);
            end                        

            // Custom Script
            if ~isempty(Tasks(j,i).SelectionScript) then
                selects = Tasks(j,i).SelectionScript;                
                try
                    url = jnewInstance(URL,selects);
                    ok = %t;
                    jremove(url);
                catch 
                    ok = %f;
                end                

                if ~ok
                    jinvoke(t_conf,'setCustomScriptUrl','file:'+selects);
                else
                    jinvoke(t_conf,'setCustomScriptUrl',selects);
                end
                jinvoke(t_conf,'setStaticScript',Tasks(j,i).Static);
                jinvoke(t_conf,'setCustomScriptParams',Tasks(j,i).ScriptParams);
            end   
            
            // Topology
            if Tasks(j,i).NbNodes > 1 then
                if ~(type(Tasks(j,i).Topology) == 10)
                    error('PAsolve::Topology is not defined in Task '+string(j)+','+string(i)+' with NbNodes > 1.');
                end
                jinvoke(t_conf,'setNbNodes',Tasks(j,i).NbNodes);
                jinvoke(t_conf,'setTopology',Tasks(j,i).Topology);
                jinvoke(t_conf,'setThresholdProximity',Tasks(j,i).ThresholdProximity);
            end  
        end
    end
endfunction

function nm=indToFile(ind)
    nm='';
    if ind==-1
        return;
    end
    for JJ=ind
        nm=strcat([nm, '_', string(JJ)]);
    end
endfunction

function inputScript = createInputScript(arg)
    // We transform the argument to executable scilab code
    argcode = sci2exp(arg,'in');
    // The inputscript will contain the argument and the code of the functions
    inputscript_array=[argcode];
    // The code defining the function is made of many lines, we pack it by using ASCII 31 delimiters
    s=size(inputscript_array);
    inputscript_str='';
    for j=1:(s(1)-1)
        inputscript_str=inputscript_str+inputscript_array(j)+ascii(31);
    end
    inputscript_str=inputscript_str+inputscript_array(s(1));
    // We tranform the final packed command as an assigment to evaluate
    inputscript_assignment = sci2exp(inputscript_str, 'inputscript');
    // We add the code used to decode the packed instructions
    inputscript_decode = 'TOKENS = tokens(inputscript,ascii(31)); execstr(TOKENS,''errcatch'',''m'');';
    inputScript = strcat([inputscript_assignment;';';inputscript_decode]);
endfunction

function mainScript = createMainScript(funcName, opt)
    debugv = opt.Debug;
    if debugv == 1
        //mainScript = strcat(['mode(3)';ascii(31);'out=';funcName;'(in)';ascii(31);ascii(30);'output = sci2exp(output,''''output'''',0)';ascii(31);'disp(out)']);
        mainScript = strcat(['mode(3)';ascii(31);'out=';funcName;'(in)';ascii(31);'disp(out)']);
    else
        //mainScript = strcat(['out=';funcName;'(in);';ascii(31);ascii(30);'output = sci2exp(output,''''output'''',0);']);
        mainScript = strcat(['out=';funcName;'(in);']);
    end
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
