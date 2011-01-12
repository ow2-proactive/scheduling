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
