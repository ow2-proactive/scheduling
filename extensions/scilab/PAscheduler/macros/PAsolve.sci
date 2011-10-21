function outputs = PAsolve(varargin)
    global ('PA_connected','DataRegistry', 'DataRegistryInit', 'SOLVEid', 'PAResult_TasksDB')

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
                    error(strcat(['parameter ', num2str(i), ' is a ', typeof(varargin(i)), ', expected PATask instead.']));
                end
            end

        end
    end

    jimport org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig;
    jimport org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;
    jimport org.ow2.proactive.scheduler.ext.common.util.FileUtils;
    jimport org.ow2.proactive.scheduler.ext.matsci.client.AODataspaceRegistry;
    jimport java.lang.String;
    jimport java.net.URL;
    jimport java.io.File;
    
    initJavaStack();
    addJavaObj(PASolveScilabGlobalConfig);
    addJavaObj(PASolveScilabTaskConfig); 
    addJavaObj(FileUtils);
    addJavaObj(AODataspaceRegistry);
    addJavaObj(String);
    addJavaObj(URL);
    addJavaObj(File);

    solve_config = jnewInstance(PASolveScilabGlobalConfig);
    addJavaObj(solve_config);

    solve_config.setDebug(opt.Debug);
    solve_config.setTimeStamp(opt.TimeStamp);
    solve_config.setFork(opt.Fork);
    solve_config.setRunAsMe(opt.RunAsMe);
    solve_config.setPriority(opt.Priority);
    solve_config.setTransferSource(opt.TransferSource);
    //solve_config.setTransferEnv(opt.TransferEnv);
    solve_config.setTransferVariables(opt.TransferVariables);
    solve_config.setKeepEngine(opt.KeepEngine);
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
            url=URL.new(selects);            
            ok = true;
        catch 
            ok = false;
        end
        jremove(url);

        if ~ok            
            solve_config.setCustomScriptUrl(strcat(['file:', selects]));
        else
            solve_config.setCustomScriptUrl(selects);
        end
    end

    solve_config.setZipSourceFiles(%f);

    curr_dir = pwd();
    fs=filesep();
    curr_dir_java = File.new(curr_dir);
    addJavaObj(curr_dir_java);
    if ~jinvoke(curr_dir_java,'canWrite')
        clearJavaStack();       
        error('Current Directory should have write access rights');
    end    
    // PAScheduler sub directory init

    subdir = '.PAScheduler';
    solve_config.setTempSubDirName(subdir);

    if isempty(opt.CustomDataspaceURL)
        if ~isdir(strcat([curr_dir, fs, subdir]))
            mkdir(curr_dir,subdir);
        end
        pa_dir = strcat([curr_dir, fs, subdir]);
    else
        if isempty(opt.CustomDataspacePath)
            clearJavaStack();    
            error('if CustomDataspaceURL is specified, CustomDataspacePath must be specified also');
        end
        if ~isdir(strcat([opt.CustomDataspacePath, fs, subdir]))
            mkdir(opt.CustomDataspacePath,subdir);
        end
        pa_dir = strcat([opt.CustomDataspacePath, fs, subdir]);
    end

    if isempty(opt.CustomDataspaceURL)                
        if ~exists('DataRegistryInit') | DataRegistryInit ~= 1
            DataRegistry = AODataspaceRegistry.new('ScilabInputSpace','ScilabOutputSpace','ScilabSession',opt.Debug);
            DataRegistryInit = 1;
        end
        pair = DataRegistry.createDataSpace(curr_dir);
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
    

    variableInFileBaseName = ['ScilabPAsolveVarIn_' string(SOLVEid)];
    variableOutFileBaseName = ['ScilabPAsolveVarOut_' string(SOLVEid)];
    outVarFiles = list(NN);
    //tmpFiles = list();
    taskFilesToClean = list();

    task_configs = jarray('org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig', NN,MM);
    addJavaObj(task_configs);
    for i=1:NN
        taskFilesToClean($+1)=list();
        for j=1:MM
            t_conf = jnewInstance(PASolveScilabTaskConfig);
            addJavaObj(t_conf);
            task_configs(i-1,j-1) = t_conf;
            if ~isempty(Tasks(j,i).Description) then
                t_conf.setDescription(Tasks(j,i).Description);
            end


            // Input Files
            if ~isempty(Tasks(j,i).InputFiles) then
                ilen = length(Tasks(j,i).InputFiles);
                if ilen > 0 then
                    inputFiles = jarray('java.lang.String', ilen);
                    addJavaObj(inputFiles);
                    filelist = Tasks(j,i).InputFiles;
                    for k=1:ilen
                        filename = filelist(k);
                        ifstr = String.new(strsubst(filename,'\','/'));
                        addJavaObj(ifstr);
                        inputFiles(k-1)=ifstr;
                    end

                    t_conf.setInputFiles(inputFiles);
                    t_conf.setInputFilesThere(%t);
                end                
            end

            // Output Files
            if ~isempty(Tasks(j,i).OutputFiles) then
                filelist = Tasks(j,i).OutputFiles;
                ilen = length(filelist);
                if ilen > 0 then
                    outputFiles = jarray('java.lang.String', ilen);
                    addJavaObj(outputFiles);
                    for k=1:ilen
                        filename = filelist(k);
                        ofstr = String.new(strsubst(filename,'\','/'));
                        addJavaObj(ofstr);
                        outputFiles(k-1)=ofstr;
                    end

                    t_conf.setOutputFiles(outputFiles);
                    t_conf.setOutputFilesThere(%t);
                end
            end

            // Custom Script
            if ~isempty(Tasks(j,i).SelectionScript) then
                selects = Tasks(j,i).SelectionScript;                
                try
                    url = URL.new(selects);
                    ok = %t;
                catch 
                    ok = %f;
                end
                jremove(url);

                if ~ok
                    t_conf.setCustomScriptUrl(strcat(['file:', selects ]));
                else
                    t_conf.setCustomScriptUrl(selects);
                end
            end   
            
            // Topology
            if Tasks(j,i).NbNodes > 1 then
                if ~(type(Tasks(j,i).Topology) == 10)
                    error('PAsolve::Topology is not defined in Task '+string(j)+','+string(i)+' with NbNodes > 1.');
                end
                t_conf.setNbNodes(Tasks(j,i).NbNodes);
                t_conf.setTopology(Tasks(j,i).Topology);
                t_conf.setThresholdProximity(Tasks(j,i).ThresholdProximity);
            end   


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
                        
                        strName = String.new(srcName);
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
            sFN = 'ScilabPAsolve_src'+string(SOLVEid)+indToFile([i j])+'.bin';
            execstr('save(pa_dir+fs+sFN,'+Func+');');
            strName = String.new(sFN);
            addJavaObj(strName);
            sourceNames(0) = strName;
            t_conf.setFunctionVarFiles(sourceNames);                
            code=[];
            taskFilesToClean(i)=lstcat(taskFilesToClean(i), list(pa_dir+fs+sFN));


            t_conf.setFunctionName(Func);
            t_conf.addSourceFile(sFN);

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
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    jimport org.objectweb.proactive.api.PAFuture;
    addJavaObj(ScilabSolver);
    addJavaObj(PAFuture);
    
    solver = jnewInstance(ScilabSolver);
    addJavaObj(solver);

    pairinfolist = solver.solve(solve_config, task_configs);
    addJavaObj(pairinfolist);
    jobinfo = jinvoke(pairinfolist,'getX');
    addJavaObj(jobinfo);
    resfutureList =  jinvoke(pairinfolist,'getY');
    addJavaObj(resfutureList);
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
        future = jinvoke(resfutureList,'get',i-1);

        results(i)=PAResult(future, taskinfo);
    end    
    PAResult_TasksDB(SOLVEid) = ftn;
    outputs = PAResL(results);
    clearJavaStack();

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
    js=list();
    JAVA_STACK=resume(js);
endfunction

function addJavaObj(obj)
    js=JAVA_STACK;
    js($+1)=obj;
    JAVA_STACK=resume(js); 
endfunction

function clearJavaStack()
    js=JAVA_STACK;
    for i=length(js):-1:1
        try
            jremove(js(i));
        catch
        end
    end
    JAVA_STACK=resume(js); 
endfunction



