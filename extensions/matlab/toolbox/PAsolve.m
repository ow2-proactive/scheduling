% PAsolve run matlab functions remotely
%
% Syntax
%
%       Basic:
%       >> results = PAsolve(func, arg_1, arg_2, ..., arg_n);
%
%       Advanced:
%       >> results = PAsolve(patask_1(1..k), patask_2(1..k), ... ,
%       PATask_n(1..k));
%       >> results = PAsolve(patask(1..n,1..k));
%
% Inputs
%
%       func - a matlab function handle
%
%       arg_k - a parameter to the "func" function if func takes only one
%       parameter as input OR a cell-array containing the list of
%       parameters to func.
%
%       patask_k - a vector of PATask objects
%
%       patask - a matrix of PATask objects
%       
%
% Description
%
%       The call to PAsolve is synchronous until the scheduler has received the 
%       information necessary to run the tasks. PAsolve returns right
%       afterwards and doesn't block matlab until the tasks have been scheduled
%       and completed.
%
%       PAsolve returns an array of objects of type PAResult. Its size matches 
%       the number of argk or pataskk given or the number of columns in the 
%       patask matrix. 
%
%       Blocking wait functions can be called on this PAResult array or on
%       a portion of this array (see PAwaitFor, PAwaitAny). Non-blocking
%       functions can also be called to know if a result is available
%       (PAisAwaited)
%
%       PAsolve is based on the principle of parametric sweep, i.e. one
%       task/many parameters (see Basic syntax). 
%
%       PAsolve can either be called by giving a function handle and a list
%       of parameters (Basic Syntax), or by providing arrays of PATask objects which
%       allows more advanced parametrization of the execution (see PATask).
%
%       The semantic of execution for PATask matrices is that each column
%       will be executed separately, and within each column each line will
%       be execute sequentially and thus will depend on the execution of
%       the previous line.
%
%       PAsolve behaviour can be configured using the PAoptions function.
%   
%
% See also
%       PAconnect, PAoptions, PAgetResults, PATask, PAResult, PAResult/PAwaitFor,
%       PAResult/PAwaitAny, PAResult/PAisAwaited
%


% /*
%   * ################################################################
%   *
%   * ProActive Parallel Suite(TM): The Java(TM) library for
%   *    Parallel, Distributed, Multi-Core Computing for
%   *    Enterprise Grids & Clouds
%   *
%   * Copyright (C) 1997-2011 INRIA/University of
%   *                 Nice-Sophia Antipolis/ActiveEon
%   * Contact: proactive@ow2.org or contact@activeeon.com
%   *
%   * This library is free software; you can redistribute it and/or
%   * modify it under the terms of the GNU Affero General Public License
%   * as published by the Free Software Foundation; version 3 of
%   * the License.
%   *
%   * This library is distributed in the hope that it will be useful,
%   * but WITHOUT ANY WARRANTY; without even the implied warranty of
%   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
%   * Affero General Public License for more details.
%   *
%   * You should have received a copy of the GNU Affero General Public License
%   * along with this library; if not, write to the Free Software
%   * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
%   * USA
%   *
%   * If needed, contact us to obtain a release under GPL Version 2 or 3
%   * or a different license than the AGPL.
%   *
%   *  Initial developer(s):               The ProActive Team
%   *                        http://proactive.inria.fr/team_members.htm
%   *  Contributor(s):
%   *
%   * ################################################################
%   * $$PROACTIVE_INITIAL_DEV$$
%   */
function results = PAsolve(varargin)

%mlock
persistent solveid
if exist('solveid','var') == 1 && isa(solveid,'int32')
    solveid = solveid + 1;
else
    solveid = int32(0);
end

% Checking the parameters
[Tasks, NN, MM]=parseParams(varargin{:});



sched = PAScheduler;
% Get the solver from memory
solver = sched.PAgetsolver();
if strcmp(class(solver),'double')
    error('This Matlab session is not connected to a Scheduler.');
end
try 
    tst = solver.isConnected();
catch ME
    error('This Matlab session is not connected to a Scheduler.');
end
if ~tst
    error('This Matlab session is not connected to a Scheduler.');
end

opt = PAoptions;
solve_config = org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig();
task_config = javaArray('org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig', NN,MM);
for i=1:NN
    for j=1:MM
        task_config(i,j)= org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig();
    end
end

% Checking all functions
[funcDatabase,allfuncs] = initFunctions(Tasks,task_config, NN, MM,sched);


% Temp directories
[globalFilesToClean,taskFilesToClean,pa_dir,curr_dir,fs,subdir] = initDirectories(opt,solve_config,NN,solveid);

% Initializing data spaces
initDS(opt,solve_config,curr_dir);

% Initialize Remote functions
[keepaliveFunctionName,checktoolboxesFunctionName]= initRemoteFunctions(solve_config);

% Transfering source files
[funcDatabase,taskFilesToClean] = initTransferSource(opt,fs,solveid,funcDatabase,sched,allfuncs,...
    NN,MM,Tasks,keepaliveFunctionName,checktoolboxesFunctionName,taskFilesToClean,task_config,pa_dir);


% Transfering the environment
envZipName = ['MatlabPAsolveEnv_' num2str(solveid) '.zip'];
envMatName = ['MatlabEnv_' num2str(solveid) '.mat'];
if opt.TransferEnv
    envFilePath = [pa_dir fs envMatName];
    envZipFilePath = [pa_dir fs envZipName];
    evalin('caller', ['save(''' envFilePath  ''',''' opt.TransferMatFileOptions ''')']);
        
    solve_config.setEnvMatFileName(envMatName);
    
    globalFilesToClean=[globalFilesToClean {envFilePath}];
end

% Init Input Files
[taskFilesToClean] = initInputFiles(NN,MM,Tasks,opt,fs,taskFilesToClean,task_config,subdir,pa_dir);

% Init Output Files
[taskFilesToClean] = initOutputFiles(NN,MM,Tasks,opt,subdir,pa_dir,taskFilesToClean,task_config);

% Init Other attributes
initOtherTCAttributes(NN,MM, task_config, Tasks);

% Init Parameters
[input,main,taskFilesToClean,outVarFiles]=initParameters(solveid,NN,MM,Tasks,opt,taskFilesToClean,task_config,allfuncs,pa_dir,fs);

% Init Solve Config
initSolveConfig(solve_config,opt,sched);

% Send the task list to the scheduler

pairinfolist = solver.solve(solve_config, task_config);

jobinfo = pairinfolist.getX();
resfutureList = pairinfolist.getY();
jid = char(jobinfo.getJobId());
disp(['Job submitted : ' jid]);

sched.PAaddDirToClean(jid, pa_dir);

ftn = jobinfo.getFinalTaskNames();
sched.PATaskRepository(jid, jobinfo);
tnit = ftn.iterator();
for i=1:NN
    taskinfo.cleanFileSet = taskFilesToClean{i};
    taskinfo.cleanDirSet = {pa_dir};
    taskinfo.outFile = outVarFiles{i};
    taskinfo.jobid = jid;
    taskinfo.taskid = char(tnit.next());
    sched.PATaskRepository(jid, taskinfo.taskid, taskinfo);
    results(i)=PAResult(resfutureList.get(i-1), taskinfo);
    for j=1:length(taskFilesToClean{i})
        sched.PAaddFileToClean(jid, taskFilesToClean{i}{j});
    end
end

if opt.AutomaticDump
    sched.dumpState();
end

end

% Parse command line parameters
function [Tasks, NN, MM]=parseParams(varargin)
if isa(varargin{1}, 'function_handle')
    Func = varargin{1};
    NN=length(varargin)-1;
    Tasks(1:NN) = PATask;
    Tasks(1:NN).Func = Func;
    for i=1:NN
        if isa(varargin{i+1}, 'PATask')
            error('PATask parameters not supported with a function_handle as first argument.');
        end
        Tasks(i).Params = varargin{i+1};
    end
    MM = 1;
elseif isa(varargin{1}, 'PATask')
    if length(varargin) == 1
        Tasks = varargin{1};
        NN = size(Tasks,2);
        MM = size(Tasks,1);
    else
        NN=length(varargin);
        MM = -1;
        for i=1:NN
            if isa(varargin{i}, 'PATask')
                if (size(varargin{i},2) ~= 1)
                    error(['parameter ' num2str(i) ' should be a column vector.']);
                end
                sz = size(varargin{i},1);
                if MM == -1
                    MM = sz;
                elseif MM ~= sz
                    error(['parameter ' num2str(i) ' should be a column vector of the same length than other parameters.']);
                end
                Tasks(1:sz,i) = varargin{i};
            else
                error(['parameter ' num2str(i) ' is a ' class(varargin{i}) ', expected PATask instead.']);
            end
        end

    end

    NN = size(Tasks,2);
else
    error(['Unsupported argument of class ' class(varargin{1})]);
end

end

% Initalize used functions (check dependencies)
function [funcDatabase,allfuncs] = initFunctions(Tasks,task_config, NN, MM,sched)
v=version;
[vmaj rem] = strtok(v, '.');
vmaj = str2num(vmaj);
vmin = strtok(rem, '.');
vmin = str2num(vmin);
funcDatabase = [];

    function strf=convertFunc(foo)
        if vmaj > 7 || vmin > 2
            try
                nargin(foo);
            catch err
                if strcmp(err.identifier,'MATLAB:nargin:isScript') == 1
                    error([char(foo) ' parameter is a script, expected a function']);
                else
                    throw(err);
                end
            end
        end
        strf = func2str(foo);
        if strf(1) ~= '@'
            strf = strcat ('@', strf);
        end
    end

    function sp = findScriptParams(foo,foostr)
        % find the list of toolboxes used by the user function and give it as parameter to the script
        if foostr(2) ~= '('
            tblist = sched.findUsedToolboxes(func2str(foo));
            tblist = union({'matlab'},tblist);
        else
            % if func is an anonymous function, we can't find dependencies
            tblist = {'matlab'}
        end
        sp = javaArray('java.lang.String',length(tblist));
        for II=1:length(tblist)
            sp(II) = java.lang.String(tblist{II});
        end

    end

for i=1:NN
    for j=1:MM
        if isa(Tasks(j,i).Func,'function_handle')
            strfunc = convertFunc(Tasks(j,i).Func);
            allfuncs(i,j).f = Tasks(j,i).Func;
            allfuncs(i,j).s = strfunc;
            if ~isfield(funcDatabase, strfunc(2:end))
                % find the list of toolboxes used by the user function and give it as parameter to the script
                sp = findScriptParams(allfuncs(i,j).f,allfuncs(i,j).s);
                funcDatabase.(strfunc(2:end)).sp = sp;
            else
                sp = funcDatabase.(strfunc(2:end)).sp;
            end
            task_config(i,j).setToolboxesUsed(sp);
        else
            error(['Parameter ' num2str(i) ',' num2str(j)  ' has no function definition.']);
        end
        
    end
end
end



% Initilize directories used
function [globalFilesToClean,taskFilesToClean,pa_dir,curr_dir,fs,subdir] = initDirectories(opt,solve_config,NN,solveid)

fs = filesep;

curr_dir = pwd;
curr_dir_java = java.io.File(curr_dir);
if ~curr_dir_java.canWrite()
    error('Current Directory should have write access rights');
end

subdir = '.PAScheduler';

if isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
    if (~exist([curr_dir fs subdir],'dir'))
        mkdir(curr_dir,subdir);
    end
    if (~exist([curr_dir fs subdir fs num2str(solveid)],'dir'))
        mkdir([curr_dir fs subdir],num2str(solveid));
    end
    pa_dir = [curr_dir fs subdir fs num2str(solveid)];
else
    if isnumeric(opt.CustomDataspacePath) && isempty(opt.CustomDataspacePath)
        error('if CustomDataspaceURL is specified, CustomDataspacePath must be specified also');
    end
    if (~exist([opt.CustomDataspacePath fs subdir],'dir'))
        mkdir(opt.CustomDataspacePath,subdir);
    end
    if (~exist([opt.CustomDataspacePath fs subdir fs num2str(solveid)],'dir'))
        mkdir([opt.CustomDataspacePath fs subdir],num2str(solveid));
    end
    pa_dir = [opt.CustomDataspacePath fs subdir fs num2str(solveid)];
end

subDirNames = javaArray('java.lang.String', 2);
subDirNames(1) = java.lang.String(subdir);
subDirNames(2) = java.lang.String(num2str(solveid));

solve_config.setTempSubDirNames(subDirNames);

globalFilesToClean = {};
taskFilesToClean=cell(1,NN);
for i=1:NN
    taskFilesToClean{i}={};
end

subdir = [subdir '/' num2str(solveid)];
end

% Initialize Data Spaces
function initDS(opt,solve_config,curr_dir)

if isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
    helper = org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.getInstance();
    pair = helper.createDataSpace(curr_dir);
    solve_config.setInputSpaceURL(pair.getX());
    solve_config.setOutputSpaceURL(pair.getY());

else
    solve_config.setOutputSpaceURL(opt.CustomDataspaceURL);
    solve_config.setInputSpaceURL(opt.CustomDataspaceURL);
end


end

% Initialize Remote Functions
function [keepaliveFunctionName,checktoolboxesFunctionName] = initRemoteFunctions(solve_config)
keepaliveFunctionName = 'keepalive_callback_fcn';
solve_config.setKeepaliveCallbackFunctionName(keepaliveFunctionName);
checktoolboxesFunctionName = 'checktoolboxes_start_and_hide_desktop';
solve_config.setChecktoolboxesFunctionName(checktoolboxesFunctionName);
end

% Initialize Global PASolve Config
function initSolveConfig(solve_config,opt,sched)
solve_config.setDebug(opt.Debug);
lgin = sched.PAgetlogin();
solve_config.setLogin(lgin);
solve_config.setTimeStamp(opt.TimeStamp);
solve_config.setPriority(opt.Priority);
solve_config.setTransferEnv(opt.TransferEnv);
solve_config.setMatFileOptions(opt.TransferMatFileOptions);
solve_config.setLicenseServerUrl(opt.LicenseServerURL);
solve_config.setFork(opt.Fork);
solve_config.setRunAsMe(opt.RunAsMe);
solve_config.setWindowsStartupOptionsAsString(opt.WindowsStartupOptions);
solve_config.setLinuxStartupOptionsAsString(opt.LinuxStartupOptions);

solve_config.setInputSpaceName('MatlabInputSpace');
solve_config.setOutputSpaceName('MatlabOutputSpace');

solve_config.setVersionPref(opt.VersionPref);
solve_config.setVersionRejAsString(opt.VersionRej);
solve_config.setVersionMin(opt.VersionMin);
solve_config.setVersionMax(opt.VersionMax);
solve_config.setCheckMatSciUrl(opt.FindMatlabScript);
solve_config.setCheckLicenceScriptUrl(opt.MatlabReservationScript);
if ischar(opt.CustomScript)
    select = opt.CustomScript
    try
        java.net.URL(select);
        ok = true;
    catch ME
        ok = false;
    end

    if ~ok
        solve_config.setCustomScriptUrl(['file:' select]);
    else
        solve_config.setCustomScriptUrl(select);
    end
end
solve_config.setZipInputFiles(opt.ZipInputFiles);
solve_config.setZipOutputFiles(opt.ZipOutputFiles);
solve_config.setZipSourceFiles(true);
solve_config.setUseMatlabControl(opt.UseMatlabControl);
end



% Initialize task config for Tansfer source (zip function used)
function [funcDatabase,taskFilesToClean] = initTransferSource(opt, fs, solveid,funcDatabase,sched,allfuncs, NN, MM,Tasks,keepaliveFunctionName,checktoolboxesFunctionName,taskFilesToClean,task_config,pa_dir)
sourceZipBaseName = ['MatlabPAsolveSrc_' num2str(solveid)];


    function  [zFN zFP]=buildZiplist(strfoo,ind,envziplist,paramziplist)
        if ~isfield(funcDatabase, strfoo(2:end)) || ~isfield(funcDatabase.(strfoo(2:end)),'dep')
            [mfiles classdirs] = sched.findDependency(strfoo(2:end));
            funcDatabase.(strfoo(2:end)).dep.mfiles = mfiles;
            funcDatabase.(strfoo(2:end)).dep.classdirs = classdirs;

        else
            mfiles = funcDatabase.(strfoo(2:end)).dep.mfiles;
            classdirs = funcDatabase.(strfoo(2:end)).dep.classdirs;
        end

        z = union(mfiles, classdirs);
        z=union(z,envziplist);
        z=union(z,paramziplist);
        [pasolvepath, pasolvename, pasolveext] = fileparts(mfilename('fullpath'));

        keepalive_cb_path = [pasolvepath, fs, 'Utils', fs, keepaliveFunctionName, '.m'];
        checktoolboxes_fn_path = [pasolvepath, fs, 'Utils', fs, checktoolboxesFunctionName, '.m'];
        z=union(z, {keepalive_cb_path,checktoolboxes_fn_path});
        bigstr = '';
        for kk = 1:length(z)
            bigstr = [bigstr z{kk}];
        end
        hashsource = char(org.ow2.proactive.scheduler.ext.common.util.IOTools.generateHash(bigstr));
        zFN = [sourceZipBaseName '_' hashsource '.zip'];
        zFP = [pa_dir fs zFN];
        if ~exist(zFP, 'file')
            zip(zFP, z);                    
        end
%         if length(z) > 0
                        
%         else
%             % Dummy code in case there is no file to zip
%             zFP = [pa_dir fs zFN];
%             zip(zFP, {[mfilename('fullpath') '.m']});
%             h = char(org.ow2.proactive.scheduler.ext.common.util.IOTools.generateHash(zFP));
%         end
    end



stdclasses = {'logical','char','int8','uint8','int16','uint16','int32','uint32','int64','uint64','single','double','cell','struct','function_handle'};
envziplist={};
if opt.TransferEnv
    sp=evalin('caller', 'whos');

    for i=1:length(sp)
        c=sp(i).class;
        if ismember(c, stdclasses) || evalin('caller', ['iscom(' sp(i).name ')']) || evalin('caller', ['isjava(' sp(i).name ')']) || evalin('caller', ['isinterface(' sp(i).name ')'])
        else
            if ~isfield(funcDatabase, c) || ~isfield(funcDatabase.(c),'dep')
                [envmfiles envclassdirs] = sched.findDependency(c);
                funcDatabase.(c).dep.mfiles = envmfiles;
                funcDatabase.(c).dep.classdirs = envclassdirs;

            else
                envmfiles = funcDatabase.(c).dep.mfiles;
                envclassdirs = funcDatabase.(c).dep.classdirs;
            end
            envziplist = union(envziplist,envmfiles);
            envziplist = union(envziplist,envclassdirs);
        end
    end
end

for i=1:NN
    for j=1:MM
        paramziplist={};

        argi = Tasks(j,i).Params;
        for k=1:length(argi)
            c=class(argi{k});
            if ismember(c, stdclasses) || iscom(argi{k}) || isjava(argi{k}) || isinterface(argi{k})
            else
                if ~isfield(funcDatabase, c) || ~isfield(funcDatabase.(c),'dep')
                    [parammfiles paramclassdirs] = sched.findDependency(c);
                    funcDatabase.(c).dep.mfiles = parammfiles;
                    funcDatabase.(c).dep.classdirs = paramclassdirs;

                else
                    parammfiles = funcDatabase.(c).dep.mfiles;
                    paramclassdirs = funcDatabase.(c).dep.classdirs;
                end

                paramziplist = union(paramziplist, parammfiles);
                paramziplist = union(paramziplist, paramclassdirs);
            end
        end
        [zFN zFP]=buildZiplist(allfuncs(i,j).s,[i j],envziplist,paramziplist);

        task_config(i,j).setSourceZipFileName(zFN);
        taskFilesToClean{i}=[taskFilesToClean{i} {zFP}];

    end
end
end

% Initialize Task Config Input Files
function [taskFilesToClean] = initInputFiles(NN,MM,Tasks,opt,fs,taskFilesToClean,task_config,subdir,pa_dir)
for i=1:NN
    for j=1:MM
        ilen = length(Tasks(j,i).InputFiles);
        if ilen > 0
            inputFiles = javaArray('java.lang.String', ilen);
            if opt.ZipInputFiles
                inputZipNames = javaArray('java.lang.String', ilen);
            end

            filelist = Tasks(j,i).InputFiles;
            for k=1:ilen
                if opt.ZipInputFiles
                    filename = filelist{k};
                    [pathstr, name, ext] = fileparts(filename);
                    inputZipName = [name ext '.zip'];
                    inputZipPath = [pa_dir fs inputZipName];
                    if exist(filename)
                        ziplist = {filename};
                        zip(inputZipPath, ziplist);
                    end
                    inputZipNames(k)=java.lang.String(inputZipName);
                    inputFiles(k)=java.lang.String([subdir '/' inputZipName]);
                    taskFilesToClean{i}=union(taskFilesToClean{i}, {inputZipPath});
                else
                    if isAbsolute(filelist{k})
                        error([filelist{k} ' is an absolute pathname, use the option "ZipInputFiles" if you want to use it.']);
                    end
                    inputFiles(k)=java.lang.String(strrep(filelist{k},'\','/'));
                end
            end
            if opt.ZipInputFiles
                task_config(i,j).setInputFilesZipNames(inputZipNames);
            end

            task_config(i,j).setInputFiles(inputFiles);
            task_config(i,j).setInputFilesThere(true);
        end
    end

end
end

% Initialize Task Config Output Files
function [taskFilesToClean] = initOutputFiles(NN,MM,Tasks,opt,subdir,pa_dir,taskFilesToClean,task_config)
outputFilesLists = [];
outputFiles = [];

for i=1:NN
    for j=1:MM
        filelist = Tasks(j,i).OutputFiles;
        noutput = length(filelist);
        if noutput > 0
            outputFiles = javaArray('java.lang.String', noutput);
            if opt.ZipOutputFiles
                outputFilesLists = javaArray('java.lang.String', noutput);
                outputZipNames= javaArray('java.lang.String', noutput);
            end

            %outputZipName = [outputZipBaseName indTofile([i j]) '.zip'];
            %outputZipPath = [subdir '/' outputZipName];

            for k=1:noutput
                filename = filelist{k};
                if isAbsolute(filename)
                    error([filename ' is an absolute pathname, invalid for output files.']);
                end

                if opt.ZipOutputFiles
                    [pathstr, name, ext] = fileparts(filename);
                    outputZipName = [name ext '.zip'];
                    outputZipNames(k)=java.lang.String(outputZipName);
                    outputZipPath = [subdir '/' outputZipName];
                    outputFilesLists(k)=java.lang.String(strrep(filename,'\','/'));
                    outputFiles(k)=java.lang.String(outputZipPath);
                    taskFilesToClean{i}=union(taskFilesToClean{i}, {[pa_dir fs outputZipName]});
                else
                    outputFiles(k)=java.lang.String(strrep(filename,'\','/'));
                end
            end
            if opt.ZipOutputFiles
                task_config(i,j).setOutputFilesZipNames(outputZipNames);
                task_config(i,j).setOutputFilesZipList(outputFilesLists);
            end
            task_config(i,j).setOutputFiles(outputFiles);
            task_config(i,j).setOutputFilesThere(true);
        end
    end
end
end

% Initialize Task Config Input Parameters
function [input,main,taskFilesToClean,outVarFiles]=initParameters(solveid,NN,MM,Tasks,opt,taskFilesToClean,task_config,allfuncs,pa_dir,fs)

input = 'i=0;';

variableInFileBaseName = ['MatlabPAsolveVarIn_' num2str(solveid)];
variableOutFileBaseName = ['MatlabPAsolveVarOut_' num2str(solveid)];

outVarFiles = cell(1,NN);
for i=1:NN
    for j=1:MM
        % Creating the input command
        % (We use this amazing contribution which converts (nearly) any variable
        % to an evaluatable string)
        argi = Tasks(j,i).Params;
        main ='';

            inVarFN = [variableInFileBaseName indToFile([i j]) '.mat'];
            outVarFN = [variableOutFileBaseName indToFile([i j]) '.mat'];
            inVarFP = [pa_dir fs inVarFN];
            outVarFP = [pa_dir fs outVarFN];
            % Creating input parameters mat files
            if length(argi) == 0
                in.in1=true;
            else
                for k=1:length(argi)
                    in.(['in' num2str(k)]) = argi{k};
                end
            end
            if (ischar(opt.TransferMatFileOptions) && length(opt.TransferMatFileOptions) > 0)
                save(inVarFP,'-struct','in',opt.TransferMatFileOptions);
            else
                save(inVarFP,'-struct','in');
            end
            taskFilesToClean{i}=union(taskFilesToClean{i}, {inVarFP});
            % because of disconnected mode, the final out is handled
            % differently
            if j < MM
                taskFilesToClean{i}=union(taskFilesToClean{i}, {outVarFP});
            end
            task_config(i,j).setInputVariablesFileName(inVarFN);
            task_config(i,j).setOutputVariablesFileName(outVarFN);
            if j > 1 && Tasks(j,i).Compose
                task_config(i,j).setComposedInputVariablesFileName([variableOutFileBaseName indToFile([i j-1]) '.mat']);
            end

            % The last task in the chain contains the final output
            if j == MM
                outVarFiles{i} = outVarFP;
            end


        % Creating the rest of the command (evaluation of the user
        % function)
        main = [main 'out = ' allfuncs(i,j).s(2:end) '('];

        if j > 1 && length(argi) > 0 && Tasks(j,i).Compose
            main = [main 'in' ','];
        elseif j > 1 && Tasks(j,i).Compose
            main = [main 'in'];
        end
        if length(argi) > 0
            for k=1:length(argi)-1
                main = [main 'in' num2str(k) ','];
            end
            main = [main 'in' num2str(length(argi))];
        end
        main = [main ');'];
        task_config(i,j).setInputScript(input);
        task_config(i,j).setMainScript(main);
        
    end
end
end

% Initialize other task config attributes
function initOtherTCAttributes(NN,MM, task_config, Tasks)
for i=1:NN
    for j=1:MM
        task_config(i,j).setDescription(Tasks(j,i).Description);
        if ischar(Tasks(j,i).SelectionScript)
            select = Tasks(j,i).SelectionScript;
            try
                java.net.URL(select);
                ok = true;
            catch ME
                ok = false;
            end
            
            if ~ok
                task_config(i,j).setCustomScriptUrl(['file:' select]);
            else
                task_config(i,j).setCustomScriptUrl(select);
            end
        end
        if Tasks(j,i).NbNodes > 1
            if ~ischar(Tasks(j,i).Topology)
                error(['PAsolve::Topology is not defined in Task ' num2str(j) ',' num2str(i) ' with NbNodes > 1.']);
            end
            task_config(i,j).setNbNodes(Tasks(j,i).NbNodes);
            task_config(i,j).setTopology(Tasks(j,i).Topology);
            task_config(i,j).setThresholdProximity(Tasks(j,i).ThresholdProximity);
        end
    end
end
end


function nm=indToFile(ind)
nm='';
if ind==-1
    return;
end
for JJ=ind
    nm=[nm '_' num2str(JJ)];
end
end

function ok=isAbsolute(file)
jf = java.io.File(file);
ok=jf.isAbsolute();
end

