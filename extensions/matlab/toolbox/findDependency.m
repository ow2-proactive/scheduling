function [mfiles classdirs] = findDependency(functionName)
global alreadyFoundFunctions
alreadyFoundFunctions = [];
functionName = lower(functionName);
[list,builtins,classes] = depfun(functionName,'-quiet','-toponly');
root = matlabroot;
list=removeStd(list);
if length(list) == 0
else
    alreadyFoundFunctions = {list{1}};
    for i=2:length(list)
        analyseFunc(list{i});
    end
end
aFF = alreadyFoundFunctions;
clear global alreadyFoundFunctions;
[mfiles classdirs] = splitClasses(aFF);
%depmodules = setdiff(depmodules, {'shared','matlab'});
%if ~length(depmodules)
%    depmodules = {'matlab'};
%end
end

function l=removeStd(list)
    l={};
    root = matlabroot;
    for i=1:length(list)
       if ~isempty(strfind(list{i},root)) 
       elseif isempty(list{i})
       else
            l=[l {list{i}}];
       end
    end
end

function analyseFunc(func)
[pathstr, name, ext] = fileparts(func);

if strfind(func,matlabroot)
else

    idx = strfind(pathstr,'@');
    if idx
        l = idx(end);
    else
        l = length(pathstr);
    end

    if strfind(pathstr(l:end), filesep)
        subFindUsed(func);
    else
        if strcmp(pathstr(l:end), name)
            met = methods(name);
            for k=1:length(met)
                subFindUsed(fullfile(pathstr, met{k}, '.m', []));
            end
        else
            subFindUsed(func);
        end
    end
end
end

function subFindUsed(functionPath)
global alreadyFoundFunctions
found = max(cellfun(@(x)strcmp(x,functionPath), alreadyFoundFunctions));
if (~found)
    alreadyFoundFunctions = union(alreadyFoundFunctions, {functionPath});
    [PATHSTR,functionName,EXT] = fileparts(functionPath);
    [list,builtins,classes] = depfun(functionPath,'-quiet','-toponly');
    list=removeStd(list);
    root = matlabroot;

    for i=2:length(list)
        analyseFunc(list{i});
    end
end

end

function [mfiles classdirs] = splitClasses(fileList)
classdirs = {};
mfiles = {};
fileList=removeStd(fileList);
for i=1:length(fileList)
    [PATHSTR,functionName,EXT] = fileparts(fileList{i});
    idx = strfind(PATHSTR,'@');
    if idx
        l = idx(end);
        if strfind(PATHSTR(l:end), filesep)
        else
            classdirs = union(classdirs, {PATHSTR});
        end
    else
        mfiles = union(mfiles,fileList{i});
    end
end
end
