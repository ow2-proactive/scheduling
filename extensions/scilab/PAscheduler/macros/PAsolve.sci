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
    clzgconf = class('org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabGlobalConfig');
    clztconf = class('org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig');
    clzdsreg = class('org.ow2.proactive.scheduler.ext.matsci.client.AODataspaceRegistry');
    clzstr = class('java.lang.String');
    clzurl = class('java.net.URL');
    clzfile= class('java.io.File');
    
    loadClass('[Ljava.lang.String;');



    solve_config = newInstance(clzgconf);

    invoke(solve_config,'setDebug',opt.Debug);
    invoke(solve_config,'setTimeStamp',opt.TimeStamp);
    invoke(solve_config,'setPriority',opt.Priority);
    invoke(solve_config,'setTransferSource',opt.TransferSource);
    //solve_config.setTransferEnv(opt.TransferEnv);
    invoke(solve_config,'setTransferVariables',opt.TransferVariables);
    invoke(solve_config,'setKeepEngine',opt.KeepEngine);

    invoke(solve_config,'setInputSpaceName','ScilabInputSpace');
    invoke(solve_config,'setOutputSpaceName','ScilabOutputSpace');

    invoke(solve_config,'setVersionPref',opt.VersionPref);
    invoke(solve_config,'setVersionRejAsString',opt.VersionRej);
    invoke(solve_config,'setVersionMin',opt.VersionMin);
    invoke(solve_config,'setVersionMax',opt.VersionMax);
    invoke(solve_config,'setCheckMatSciUrl',opt.FindScilabScript);
    if ischar(opt.CustomScript)
        selects = opt.CustomScript
        try
            newInstance(clzurl,selects);
            ok = true;
        catch 
            ok = false;
        end

        if ~ok
            invoke(solve_config,'setCustomScriptUrl',strcat(['file:', selects]));
        else
            invoke(solve_config,'setCustomScriptUrl',selects);
        end
    end
    //solve_config.setZipInputFiles(opt.ZipInputFiles);
    //solve_config.setZipOutputFiles(opt.ZipOutputFiles);
    invoke(solve_config,'setZipSourceFiles',%f);

    curr_dir = pwd();
    fs=filesep();
    curr_dir_java = newInstance(clzfile,curr_dir);
    if ~invoke_u(curr_dir_java,'canWrite')
        error('Current Directory should have write access rights');
    end

    // PAScheduler sub directory init

    subdir = '.PAScheduler';
    invoke(solve_config,'setTempSubDirName',subdir);

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
            DataRegistry = newInstance(clzdsreg,'ScilabInputSpace','ScilabOutputSpace','ScilabSession',opt.Debug);            
            DataRegistryInit = 1;
        end
        pair = invoke(DataRegistry,'createDataSpace',curr_dir);
        px=invoke(pair,'getX');        
        py=invoke(pair,'getY');
        pxs = javaCast(px,'java.lang.String');
        pys = javaCast(py,'java.lang.String');
        invoke(solve_config,'setInputSpaceURL',pxs);
        invoke(solve_config,'setOutputSpaceURL',pys);
        if opt.Debug then
            disp('using Dataspace:')
            disp(pxs)
        end
        
        //solve_config.setInputSpaceURL(px);
        //solve_config.setOutputSpaceURL(py);

    else
        invoke(solve_config,'setOutputSpaceURL',opt.CustomDataspaceURL);
        invoke(solve_config,'setInputSpaceURL',opt.CustomDataspaceURL);
    end


    variableInFileBaseName = ['ScilabPAsolveVarIn_' string(SOLVEid)];
    variableOutFileBaseName = ['ScilabPAsolveVarOut_' string(SOLVEid)];
    outVarFiles = list(NN);
    tmpFiles = list();

    task_configs = javaArray('org.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig', int32(NN),int32(MM));
    for i=1:NN
        for j=1:MM
            t_conf = newInstance(clztconf);
            task_configs(i,j) = t_conf;
            if isfield(Tasks(j,i), 'Description') then
                invoke(t_conf,'setDescription',Tasks(j,i).Description);
            end


            // Input Files
            if isfield(Tasks(j,i), 'InputFiles') then
                ilen = size(Tasks(j,i).InputFiles,1);
                if ilen > 0 then
                    inputFiles = javaArray('java.lang.String', int32(ilen));
                    filelist = Tasks(j,i).InputFiles;
                    for k=1:ilen
                        filename = filelist(k);
                        inputFiles(k,1)=newInstance(clzstr,strsubst(filename,'\','/'));
                    end
                    inputFilesJArrayObj = invoke(inputFiles,'getArray');
                    inputFilesJArray = javaCast(inputFilesJArrayObj,'[Ljava.lang.String;');
                    invoke(t_conf,'setInputFiles',inputFilesJArray);
                    invoke(t_conf,'setInputFilesThere',%t);
                end                
            end

            // Output Files
            if isfield(Tasks(j,i), 'OutputFiles') then
                filelist = Tasks(j,i).OutputFiles;
                ilen = size(filelist,1);
                if ilen > 0 then
                    outputFiles = javaArray('java.lang.String', int32(ilen));
                    for k=1:ilen
                        filename = filelist(k);
                        outputFiles(k,1)=newInstance(clzstr,strsubst(filename,'\','/'));
                    end
                    outputFilesJArrayObj = invoke(outputFiles,'getArray');
                    outputFilesJArray = javaCast(outputFilesJArrayObj,'[Ljava.lang.String;');
                    invoke(t_conf,'setOutputFiles',outputFilesJArray);
                    invoke(t_conf,'setOutputFilesThere',%t);
                end
            end

            // Custom Script
            if isfield(Tasks(j,i), 'SelectionScript') then
                selects = Tasks(j,i).SelectionScript;                
                try
                    newInstance(clzurl,selects);
                    ok = true;
                catch 
                    ok = false;
                end

                if ~ok
                    invoke(t_conf,'setCustomScriptUrl',strcat(['file:', selects ]));
                else
                    invoke(t_conf,'setCustomScriptUrl',selects);
                end
            end   


            // Function
            Func = Tasks(j,i).Func;
            execstr(strcat(['functype = type(';Func;');']));
            if (functype <> 13) & (functype <> 11) then
                error(strcat(['invalid function type:', Func, ', it cannot be a C or Fortran built-in function)']));
            end

            // Source Files
            if opt.TransferSource then
                if isfield(Tasks(j,i), 'Sources') then
                    srcs = Tasks(j,i).Sources;
                    if type(srcs) ~= 15 then
                        srcs=list(srcs);
                    end
                    sourceNames = javaArray('java.lang.String', int32(length(srcs)));
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
                        strName = newInstance(clzstr,srcName);
                        sourceNames(k,1) = strName; 
                    end
                    sourceNamesJArrayObj = invoke(sourceNames,'getArray');
                    sourceNamesJArray = javaCast(sourceNamesJArrayObj,'[Ljava.lang.String;');
                    invoke(t_conf,'setSourceNames',sourceNamesJArray);

                    code=[];
                else
                    code=funccode(Func, []);
                end
            elseif isfield(Tasks(j,i), 'Sources') then
                srcs = Tasks(j,i).Sources;
                if type(srcs) == 15 then
                    error('Sources field cannot be a list if TransterSource is set to off');
                end
                if ~isfile(srcs) then
                    error(strcat(['Source file ', srcs, ' cannot be found'])); 
                end
                code=funccode(Func, srcs);
            else
                code=funccode(Func, []);
            end

            if ~isempty(code) then
                flatcode=flatten(code);
                invoke(t_conf,'setFunctionDefinition',flatcode);                            
            end

            invoke(t_conf,'setFunctionName',Func);            

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
                invoke(t_conf,'setInputVariablesFileName',inVarFN);                
                invoke(t_conf,'setOutputVariablesFileName',outVarFN);
                if j > 1 & Tasks(j,i).Compose
                    invoke(t_conf,'setComposedInputVariablesFileName',strcat([variableOutFileBaseName, indToFile([i j-1]), '.mat']));
                end
                outVarFiles(i) = outVarFP;
                tmpFiles($+1)=inVarFP;
                tmpFiles($+1)=outVarFP;
                inputscript = 'i=0';
            else
                inputscript = createInputScript(argi);
            end            

            invoke(t_conf,'setInputScript',inputscript);

            mainScript = createMainScript(Func, opt);
            invoke(t_conf,'setMainScript',mainScript);

            invoke(t_conf,'setOutputs','out');

        end
    end
    clzsolver = class('org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver');       
    solver = newInstance(clzsolver);   
    tconfJArrayObj = invoke(task_configs,'getArray'); 
    tconfJArray = javaCast(tconfJArrayObj,'[[Lorg.ow2.proactive.scheduler.ext.scilab.common.PASolveScilabTaskConfig;');
    futureList = invoke(solver,'solve',solve_config, tconfJArray);
    pafuture = class('org.objectweb.proactive.api.PAFuture');
    answerList = pafuture.getFutureValue(futureList);
    answerListL = javaCast(answerList,'java.util.ArrayList');    
    outputs=list(NN);
    ispaerror = %f;
    errormsg = '';
    for i=0:NN-1
        ral = invoke(answerListL,'get',i);
        rall = javaCast(ral,'org.ow2.proactive.scheduler.ext.scilab.client.ScilabResultsAndLogs');        
        if invoke_u(rall,'isOK') then
            if opt.TransferVariables then
                logs = invoke_u(rall,'getLogs');
                disp(logs);
                if isfile(outVarFiles(i+1)) then
                    load(outVarFiles(i+1));
                    outputs(i+1)=out;
                else
                    errormsg = strcat(['Error while receiving output n°',string(i+1), ', cannot find file ',outVarFiles(i+1)]);
                    ispaerror = %t;
                end
                
            else
                logs = invoke_u(rall,'getLogs');
                disp(logs);
                st = invoke(rall,'getResult');  
                stt = javaCast(st,'org.scilab.modules.types.ScilabType');
                execstr(strcat(['output=',invoke_u(stt,'toString')]));                          
                if exists(output) then
                    outputs(i+1)= output;
                    clear('output');
                else                    
                    errormsg = strcat(['Error while receiving output n°',string(i+1)]);
                    ispaerror = %t;
                end
                
                
            end
             
        elseif invoke_u(rall,'isMatSciError') then
            logs = invoke_u(rall,'getLogs');
            if ~isempty(logs) then
               disp(logs); 
            end
            ispaerror = %t; 
        else
            logs = invoke_u(rall,'getLogs');
            if ~isempty(logs) then
               disp(logs); 
            end
            
            ex = invoke(rall,'getException');
            exx = javaCast(ex, 'java.lang.Exception');
            exstr = invoke_u(solver, 'getStackTrace',exx);
            disp(exstr);  
            ispaerror = %t;          
        end
    end
    
    rmdir(pa_dir,'s')
//    for i=1:length(tmpFiles)
//        try
//            mdelete(tmpFiles(i))
//        catch 
//        end        
//    end
    if (ispaerror) then
        if isempty(errormsg) then
            error('Error while reading PAsolve results'); 
        else
            error(errormsg);
        end       
    end

    


    //    out = list();
    //    for i=1:max(size(results))
    //        execstr(results(i));
    //        // We should have an output variable created
    //        out($+1)=output;
    //    end


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

function flatcode=flatten(code)
    flatcode='';
    s=size(code);
    for i=1:(s(1)-1)
        flatcode=flatcode+code(i)+ascii(31);
    end
    flatcode=flatcode+code(s(1));
endfunction

function code=funccode(funcname, filedef)
    if filedef ~= [] then
        code = [];
        // Read file function definition into string
        code = [code;mgetl(file1)];
    else    
        execline = strcat(['code = fun2string(';funcname;',''';funcname;''');'])
        execstr(execline);
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



