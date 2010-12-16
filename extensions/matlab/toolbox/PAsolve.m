%   PAsolve() - run matlab functions remotely
%
%   The call to PAsolve is synchronous until the scheduler has received the
%   information necessary to run the tasks. PAsolve returns right
%   afterwards and doesn't block matlab until the tasks have been scheduled
%   and completed.
%
%   PAsolve is based on the principle of parametric sweep, i.e. one
%   task/many parameters (see Basic syntax).
%
%   In addition, it allows to define and run simplified "column" workflows
%   (see Advanced syntax).
%
%
%
%
%   Usage:
%
%       Basic syntax:
%       >> results = PAsolve(func, arg1, arg2, ...);
%
%       where :
%
%           func : a handle to a function with only one return value (but
%               can have several input parameters)
%
%           arg1, arg2, ... :
%
%
%       Advanced syntax:
%       >> results = PAsolve(PATask1(1..k), PATask2(1..k), ... , PATaskn(1..k));
%
%       or
%
%       >> results = PAsolve(PATask(1..n,1..k));
%
%   Inputs:
%       func - a handle to a Matlab or user-defined function
%       args - a one-dimensional cell array of objects holding the parameters. If the cell
%              contains X elements, then X tasks will be deployed.
%               Each cell must contain another cell array corresponding to the function multiple parameters.
%               If the function takes a single parameter, then the nested cell arrays must be of size one each
%
%       debug - '-debug' if the computation needs to be run in debug mode
%
%   Ouputs:
%       results - a cell array containing the results. The cell array will be of the same size as the input cell array.
%
%   Example: results = PAsolve(@factorial,{1}, {2}, {3}, {4}, {5})
%           Calls on remote machines factorial(1), factorial(2), etc...
%
%/*
% * ################################################################
% *
% * ProActive: The Java(TM) library for Parallel, Distributed,
% *            Concurrent computing with Security and Mobility
% *
% * Copyright (C) 1997-2010 INRIA/University of Nice-Sophia Antipolis
% * Contact: proactive@ow2.org
% *
% * This library is free software; you can redistribute it and/or
% * modify it under the terms of the GNU General Public License
% * as published by the Free Software Foundation; either version
% * 2 of the License, or any later version.
% *
% * This library is distributed in the hope that it will be useful,
% * but WITHOUT ANY WARRANTY; without even the implied warranty of
% * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
% * General Public License for more details.
% *
% * You should have received a copy of the GNU General Public License
% * along with this library; if not, write to the Free Software
% * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
% * USA
% *
% *  Initial developer(s):               The ProActive Team
% *                        http://proactive.inria.fr/team_members.htm
% *  Contributor(s):
% *
% * ################################################################
% */
function results = PAsolve(varargin)

%mlock
persistent solveid
if exist('solveid','var') == 1 && isa(solveid,'int32')
    solveid = solveid + 1;
else
    solveid = int32(0);
end

if isa(varargin{1}, 'function_handle')
    Func = varargin{1};
    NN=nargin-1;
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
    if nargin == 1
        Tasks = varargin{1};
        NN = size(Tasks,2);
        MM = size(Tasks,1);
    else
        NN=nargin;
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



% Checking arguments
% if ~isa(solver, 'pa.stub.org.ow2.proactive.scheduler.ext.matlab.embedded._StubAOMatlabEnvironment')
%     error('solver parameter should be a connection to the proactive scheduler obtained from the PAconnect function');
% end
sched = PAScheduler;
% Get the solver from memory
solver = sched.PAgetsolver();
if strcmp(class(solver),'double')
    error('connexion to the scheduler is not established');
end

opt = PAoptions;
solve_config = org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig();
task_config = javaArray('org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig', NN,MM);
for i=1:NN
    for j=1:MM
        task_config(i,j)= org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig();
    end
end
v=version;
[vmaj rem] = strtok(v, '.');
vmaj = str2num(vmaj);
vmin = strtok(rem, '.');
vmin = str2num(vmin);

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
        else
            % if func is an anonymous function, we can't find dependencies
            tblist = {'matlab'}
        end
        sp = javaArray('java.lang.String',length(tblist));
        for II=1:length(tblist)
            sp(II) = java.lang.String(tblist{II});
        end

    end

%if exist('Func','var') == 1 && isa(Func,'function_handle')
%    globalFuncExists=true;
%    StrFunc = convertFunc(Func);
%    sp = findScriptParams(Func,StrFunc);
%    solve_config.setScriptParams(sp);
%end
for i=1:NN
    for j=1:MM
        if isa(Tasks(j,i).Func,'function_handle')
            allfuncs(i,j).f = Tasks(j,i).Func;
            allfuncs(i,j).s = convertFunc(Tasks(j,i).Func);
            % find the list of toolboxes used by the user function and give it as parameter to the script
            sp = findScriptParams(allfuncs(i,j).f,allfuncs(i,j).s);
            task_config(i,j).setCheckLicenceScriptParams(sp);
        else
            error(['Parameter ' num2str(i) ',' num2str(j)  ' has no function definition.']);
        end
        task_config(i,j).setDescription(Tasks(j,i).Description);
    end
end


% Temp directories
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
    pa_dir = [curr_dir fs subdir];
else
    if isnumeric(opt.CustomDataspacePath) && isempty(opt.CustomDataspacePath)
        error('if CustomDataspaceURL is specified, CustomDataspacePath must be specified also');
    end
    if (~exist([opt.CustomDataspacePath fs subdir],'dir'))
        mkdir(opt.CustomDataspacePath,subdir);
    end
    pa_dir = [opt.CustomDataspacePath fs subdir];
end

solve_config.setTempSubDirName(subdir);

globalFilesToClean = {};
taskFilesToClean=cell(1,NN);
for i=1:NN
    taskFilesToClean{i}={};
end

% Initializing data spaces

if isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
    helper = org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.getInstance();
    pair = helper.createDataSpace(curr_dir);
    solve_config.setInputSpaceURL(pair.getX());
    solve_config.setOutputSpaceURL(pair.getY());

else
    solve_config.setOutputSpaceURL(opt.CustomDataspaceURL);
    solve_config.setInputSpaceURL(opt.CustomDataspaceURL);
end



% Transfering source files
sourceZipBaseName = ['MatlabPAsolveSrc_' num2str(solveid)];

    function nm=indToFile(ind)
        nm='';
        if ind==-1
            return;
        end
        for JJ=ind
            nm=[nm '_' num2str(JJ)];
        end
    end

    function  [zFN zFP h]=buildZiplist(strfoo,ind,envziplist,paramziplist)
        [mfiles classdirs] = sched.findDependency(strfoo(2:end));
        z = union(mfiles, classdirs);
        z=union(z,envziplist);
        z=union(z,paramziplist);
        zFN = [sourceZipBaseName indToFile(ind) '.zip'];
        if length(z) > 0
            zFP = [pa_dir fs zFN];
            zip(zFP, z);
            h = char(org.ow2.proactive.scheduler.ext.common.util.IOTools.generateHash(zFP));
        else
            % Dummy code in case there is no file to zip
            zFP = [pa_dir fs zFN];
            zip(zFP, {[mfilename('fullpath') '.m']});
            h = char(org.ow2.proactive.scheduler.ext.common.util.IOTools.generateHash(zFP));
        end
    end


if opt.TransferSource
    stdclasses = {'logical','char','int8','uint8','int16','uint16','int32','uint32','int64','uint64','single','double','cell','struct','function_handle'};
    envziplist={};
    if opt.TransferEnv
        sp=evalin('caller', 'whos');

        for i=1:length(sp)
            c=sp(i).class;
            if ismember(c, stdclasses) || evalin('caller', ['iscom(' sp(i).name ')']) || evalin('caller', ['isjava(' sp(i).name ')']) || evalin('caller', ['isinterface(' sp(i).name ')'])
            else
                [envmfiles envclassdirs] = sched.findDependency(c);
                envziplist = union(envmfiles, envclassdirs);
            end
        end
    end

    for i=1:NN
        for j=1:MM
            paramziplist={};
            if opt.TransferVariables
                argi = Tasks(j,i).Params;
                for k=1:length(argi)
                    c=class(argi{k});
                    if ismember(c, stdclasses) || iscom(argi{k}) || isjava(argi{k}) || isinterface(argi{k})
                    else
                        [parammfiles paramclassdirs] = sched.findDependency(c);
                        paramziplist = union(parammfiles, paramclassdirs);
                    end
                end
            end
            [zFN zFP h]=buildZiplist(allfuncs(i,j).s,[i j],envziplist,paramziplist);

            task_config(i,j).setSourceZipHash(h);
            task_config(i,j).setSourceZipFileName(zFN);
            taskFilesToClean{i}=[taskFilesToClean{i} {zFP}];

        end
    end
end

% Transfering the environment
envZipName = ['MatlabPAsolveEnv_' num2str(solveid) '.zip'];
envMatName = ['MatlabEnv_' num2str(solveid) '.mat'];
if opt.TransferEnv
    envFilePath = [pa_dir fs envMatName];
    envZipFilePath = [pa_dir fs envZipName];
    evalin('caller', ['save(''' envFilePath  ''',''' opt.TransferEnvMatFileOptions ''')']);
    solve_config.setZipEnvFile(opt.ZipEnvFile);
    if opt.ZipEnvFile
        zip(envZipFilePath, envFilePath);
        solve_config.setEnvZipFileName(envZipName);
        globalFilesToClean=[globalFilesToClean {envZipFilePath}];
    else
        solve_config.setEnvMatFileName(envMatName);
    end
    globalFilesToClean=[globalFilesToClean {envFilePath}];
end

% Building Input/Output Matrix



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
%end

% Output Files
outputFilesLists = [];
outputFiles = [];
%if length(p.Results.OutputFiles) > 0
%    if length(p.Results.OutputFiles) ~= NN
%        error('length of args and OutputFiles cells should match');
%    end
%outputZipBaseName = ['MatlabOutputFiles_' num2str(solveid)];

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
%end

% scattering the parameter matrix


% Creating the tasks

if opt.TransferSource
    % to optimize with Hash
    input = 'i=0;';
else
    input = strcat('restoredefaultpath;addpath(''',getUserPath,''');');
end

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
        if opt.TransferVariables
            inVarFN = [variableInFileBaseName indToFile([i j]) '.mat'];
            outVarFN = [variableOutFileBaseName indToFile([i j]) '.mat'];
            inVarFP = [pa_dir fs inVarFN];
            outVarFP = [pa_dir fs outVarFN];
            % Creating input parameters mat files
            for k=1:length(argi)
                in.(['in' num2str(k)]) = argi{k};
            end
            if (ischar(opt.TransferMatFileOptions) && length(opt.TransferMatFileOptions) > 0)
                save(inVarFP,'-struct','in',opt.TransferMatFileOptions);
            else
                save(inVarFP,'-struct','in');
            end
            taskFilesToClean{i}=union(taskFilesToClean{i}, {inVarFP});
            %% because of disconnected mode, the final out is handled
            %% differently
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

        else
            for k=1:length(argi)
                main = [main 'in' num2str(k) ' = ' sched.serialize(argi{k}) ';'];
            end
        end

        % Creating the rest of the command (evaluation of the user function)

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
    end
end


% send the task list to the scheduler

solve_config.setDebug(opt.Debug);
solve_config.setTimeStamp(opt.TimeStamp);
solve_config.setPriority(opt.Priority);
solve_config.setTransferSource(opt.TransferSource);
solve_config.setTransferEnv(opt.TransferEnv);
solve_config.setTransferVariables(opt.TransferVariables);
solve_config.setMatFileOptions(opt.TransferMatFileOptions);
solve_config.setKeepEngine(opt.KeepEngine);

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

pairinfolist = solver.solve(solve_config, task_config);

jobinfo = pairinfolist.getX();
resfutureList = pairinfolist.getY();
jid = char(jobinfo.getJobId());
disp(['Job submitted : ' jid]);
%sched.PAJobInfo(jobinfo.getJobId(), jobinfo);
sched.PAaddDirToClean(jid, pa_dir);
%taskinfos = {};
ftn = jobinfo.getFinalTaskNames();
sched.PATaskRepository(jid, jobinfo);
tnit = ftn.iterator();
for i=1:NN
    taskinfo.cleanFileSet = taskFilesToClean{i};
    taskinfo.cleanDirSet = {pa_dir};
    taskinfo.transferVariables = opt.TransferVariables;
    taskinfo.outFile = outVarFiles{i};
    taskinfo.jobid = jid;
    taskinfo.taskid = char(tnit.next());
    sched.PATaskRepository(jid, taskinfo.taskid, taskinfo);
    results(i)=PAResult(resfutureList.get(i-1), taskinfo);
    for j=1:length(taskFilesToClean{i})
        sched.PAaddFileToClean(jid, taskFilesToClean{i}{j});
    end
end

%sched.PAJobInfo(jid, taskinfos);

end

function ok=isAbsolute(file)
jf = java.io.File(file);
ok=jf.isAbsolute();
end

