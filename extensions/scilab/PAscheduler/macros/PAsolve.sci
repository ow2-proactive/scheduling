function outputs = PAsolve(varargin)
    global ('PA_connected','DataRegistry', 'DataRegistryInit', 'SOLVEid')

    if ~exists('PA_connected') | PA_connected ~= 1
        error('A connection to the ProActive scheduler must be established in order to use PAsolve, see PAconnect');
    end

    if exists('SOLVEid') & type(SOLVEid) == 8
        SOLVEid = SOLVEid + 1;
    else
        SOLVEid = int32(0);
    end



    opt=PAoptions();

    deff ("y=ischar(x)","y=type(x)==10","n");
    if ischar(varargin(1)) then
        Func = varargin(1);
        NN=length(varargin)-1;        
        Tasks(1,1:NN).Func = Func;
        for i=1:NN            
            Tasks(1,i).Params = varargin(i+1);
        end
        MM = 1;
    elseif isstruct(varargin(1))
        if length(varargin) == 1
            Tasks = varargin(1);
            NN = size(Tasks,2);
            MM = size(Tasks,1);
        else
            NN=nargin;
            MM = -1;
            for i=1:NN
                if isstruct(varargin(i))
                    if (size(varargin(i),2) ~= 1)
                        error(strcat(['parameter ', string(i), ' should be a column vector.']));
                    end
                    sz = size(varargin(i),1);
                    if MM == -1
                        MM = sz;
                    elseif MM ~= sz
                        error(strcat(['parameter ', string(i), ' should be a column vector of the same length than other parameters.']));
                    end
                    Tasks(1:sz,i) = varargin(i);
                else
                    error(strcat(['parameter ', num2str(i), ' is a ', typeof(varargin(i)), ', expected struct instead.']));
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

    solve_config = jnewInstance(PASolveScilabGlobalConfig);

    solve_config.setDebug(opt.Debug);
    solve_config.setTimeStamp(opt.TimeStamp);
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
    if ~jinvoke(curr_dir_java,'canWrite')
        jremove(curr_dir_java);
        error('Current Directory should have write access rights');
    end
    jremove(curr_dir_java);

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
        px=jinvoke(pair,'getX');
        py=jinvoke(pair,'getY');
        pxs = jcast(px,'java.lang.String');
        pys = jcast(py,'java.lang.String');
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
    tmpFiles = list();

    task_configs = jarray('org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig', NN,MM);
    for i=1:NN
        for j=1:MM
            t_conf = jnewInstance(PASolveScilabTaskConfig);
            task_configs(i-1,j-1) = t_conf;
            if isfield(Tasks(j,i), 'Description') then
                t_conf.setDescription(Tasks(j,i).Description);
            end


            // Input Files
            if isfield(Tasks(j,i), 'InputFiles') then
                ilen = size(Tasks(j,i).InputFiles,1);
                if ilen > 0 then
                    inputFiles = jarray('java.lang.String', ilen);
                    filelist = Tasks(j,i).InputFiles;
                    for k=1:ilen
                        filename = filelist(k);
                        inputFiles(k-1)=String.new(strsubst(filename,'\','/'));
                    end

                    t_conf.setInputFiles(inputFiles);
                    t_conf.setInputFilesThere(%t);
                end                
            end

            // Output Files
            if isfield(Tasks(j,i), 'OutputFiles') then
                filelist = Tasks(j,i).OutputFiles;
                ilen = size(filelist,1);
                if ilen > 0 then
                    outputFiles = jarray('java.lang.String', ilen);
                    for k=1:ilen
                        filename = filelist(k);
                        outputFiles(k-1)=String.new(strsubst(filename,'\','/'));
                    end

                    t_conf.setOutputFiles(outputFiles);
                    t_conf.setOutputFilesThere(%t);
                end
            end

            // Custom Script
            if isfield(Tasks(j,i), 'SelectionScript') then
                selects = Tasks(j,i).SelectionScript;                
                try
                    url = URL.new(selects);
                    ok = true;
                catch 
                    ok = false;
                end
                jremove(url);

                if ~ok
                    t_conf.setCustomScriptUrl(strcat(['file:', selects ]));
                else
                    t_conf.setCustomScriptUrl(selects);
                end
            end   


            // Function
            Func = Tasks(j,i).Func;
            execstr(strcat(['functype = type(';Func;');']));
            if (functype <> 13) & (functype <> 11) then
                error(strcat(['invalid function type:', Func, ', it cannot be a C or Fortran built-in function)']));
            end

            // Source Files

            if isfield(Tasks(j,i), 'Sources') then
                srcs = Tasks(j,i).Sources;
                if type(srcs) ~= 15 then
                    srcs=list(srcs);
                end
                sourceNames = jarray('java.lang.String', length(srcs));
                for k=1:length(srcs)
                    srcPath = srcs(k);
                    if isfile(srcPath) then
                        [ppath,fname,extension]=fileparts(srcPath);
                        srcName = strcat(strcat([fname,extension]));
                        if ~isfile(strcat([pa_dir,fs,fname,extension])) then
                            if opt.Debug then
                                disp(strcat(['Copying file ', srcPath, ' to ',pa_dir]));
                            end
                            copyfile(srcPath,pa_dir);
                            tmpFiles($+1)=strcat([pa_dir,fs,fname,extension]);
                        end
                    else
                        error(strcat(['Source file ', srcPath, ' cannot be found']));
                    end
                    strName = String.new(srcName);
                    sourceNames(k-1) = strName;
                end

                t_conf.setSourceNames(sourceNames);

                code=[];
            else
                sourceNames = jarray('java.lang.String', 1);
                execstr('save(pa_dir+fs+''PAsolve.bin'','+Func+');');
                strName = String.new('PAsolve.bin');
                sourceNames(0) = strName;
                t_conf.setFunctionVarFiles(sourceNames);
                t_conf.setSourceNames(sourceNames);
                code=[];
            end

            t_conf.setFunctionName(Func);

            // Params
            argi = Tasks(j,i).Params;
            if opt.TransferVariables
                inVarFN = strcat([variableInFileBaseName, indToFile([i j]), '.dat']);
                outVarFN = strcat([variableOutFileBaseName, indToFile([i j]), '.dat']);
                inVarFP = strcat([pa_dir, fs, inVarFN]);
                outVarFP = strcat([pa_dir, fs, outVarFN]);
                // Creating input parameters mat files
                in=argi;
                save(inVarFP,in);
                jinvoke(t_conf,'setInputVariablesFileName',inVarFN);
                jinvoke(t_conf,'setOutputVariablesFileName',outVarFN);
                if j > 1 & Tasks(j,i).Compose
                    jinvoke(t_conf,'setComposedInputVariablesFileName',strcat([variableOutFileBaseName, indToFile([i j-1]), '.mat']));
                end
                outVarFiles(i) = outVarFP;
                tmpFiles($+1)=inVarFP;
                tmpFiles($+1)=outVarFP;
                inputscript = 'i=0';
            else
                inputscript = createInputScript(argi);
            end            

            t_conf.setInputScript(inputscript);

            mainScript = createMainScript(Func, opt);
            t_conf.setMainScript(mainScript);

            t_conf.setOutputs('out');

        end
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    jimport org.objectweb.proactive.api.PAFuture;
    solver = jnewInstance(ScilabSolver);

    futureList = solver.solve(solve_config, task_configs);

    answerListObj = PAFuture.getFutureValue(futureList);

    outputs=list(NN);
    ispaerror = %f;
    errormsg = '';
    for i=0:NN-1
        answerList = jcast(answerListObj, 'java.util.ArrayList');
        ralObj = answerList.get(i);
        ral = jcast(ralObj,'org.ow2.proactive.scheduler.ext.scilab.client.ScilabResultsAndLogs');
        if jinvoke(ral,'isOK') then
            if opt.TransferVariables then
                logs = jinvoke(ral,'getLogs');
                printf('%s',logs);
                if isfile(outVarFiles(i+1)) then
                    load(outVarFiles(i+1));
                    outputs(i+1)=out;
                else
                    errormsg = strcat(['Error while receiving output n°',string(i+1), ', cannot find file ',outVarFiles(i+1)]);
                    ispaerror = %t;
                end

            else
                logs = jinvoke(ral,'getLogs');
                printf('%s',logs);
                st = jinvoke(ral,'getResult');

                execstr(strcat(['output=',jinvoke(st,'toString')]));
                if exists(output) then
                    outputs(i+1)= output;
                    clear('output');
                else                    
                    errormsg = strcat(['Error while receiving output n°',string(i+1)]);
                    ispaerror = %t;
                end


            end

        elseif jinvoke(ral,'isMatSciError') then
            logs = jinvoke(ral,'getLogs');
            if ~isempty(logs) then
                printf('%s',logs);
            end
            ispaerror = %t; 
        else
            logs = jinvoke(ral,'getLogs');
            if ~isempty(logs) then
                printf('%s',logs);
            end

            ex = jinvoke(ral,'getException');

            exstr = solver.getStackTrace(ex);
            disp(exstr);  
            ispaerror = %t;          
        end
        printf('\n');
    end

    rmdir(pa_dir,'s')


    jremove(task_configs,solve_config,solver);

    if (ispaerror) then
        if isempty(errormsg) then
            error('Error while reading PAsolve results'); 
        else
            error(errormsg);
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



