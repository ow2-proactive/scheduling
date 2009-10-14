function depmodules = findUsedToolboxes(functionName)
global alreadyFoundFunctions

functionName = lower(functionName);
[list,builtins,classes] = depfun(functionName,'-quiet','-toponly');
root = matlabroot;
alreadyFoundFunctions = {functionName};
depmodules = {};
toolboxroot = [ matlabroot filesep 'toolbox' filesep ];
for i=1:length(list)
    [pathstr, name, ext, versn] = fileparts(list{i});
    if ~strcmp(name,functionName) && length(name) > 0
        if strfind(list{i},matlabroot)
            pathwithouttoolboxroot = list{i}(length(toolboxroot)+1:length(list{i}));
            pos = strfind(pathwithouttoolboxroot,filesep);
            toolboxName = pathwithouttoolboxroot(1:pos-1);
            depmodules = union(depmodules, toolboxName);
        else
            subdepmodules = subFindUsed(name);
            depmodules = union(depmodules, subdepmodules);
        end
    end
end
clear alreadyFoundFunctions;
depmodules = setdiff(depmodules, {'shared','matlab'});
if ~length(depmodules)
    depmodules = {'matlab'};
end
end

function depmodules = subFindUsed(functionName)
global alreadyFoundFunctions
functionName = lower(functionName);
found = max(cellfun(@(x)strcmp(x,functionName), alreadyFoundFunctions));
if (~found)
    alreadyFoundFunctions = union(alreadyFoundFunctions, {functionName});
    [list,builtins,classes] = depfun(functionName,'-quiet','-toponly');
    root = matlabroot;

    depmodules = {};
    toolboxroot = [ matlabroot filesep 'toolbox' filesep ];
    for i=1:length(list)
        [pathstr, name, ext, versn] = fileparts(list{i});
        if ~strcmp(name,functionName)
            if strfind(list{i},matlabroot)
                pathwithouttoolboxroot = list{i}(length(toolboxroot)+1:length(list{i}));
                pos = strfind(pathwithouttoolboxroot,filesep);
                toolboxName = pathwithouttoolboxroot(1:pos-1);
                depmodules = union(depmodules, toolboxName);
            else
                subdepmodules = subFindUsed(name);
                depmodules = union(depmodules, subdepmodules);
            end
        end
    end
else
    depmodules = {};
end

end