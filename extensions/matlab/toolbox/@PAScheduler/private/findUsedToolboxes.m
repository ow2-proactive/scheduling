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
function depmodules = findUsedToolboxes(functionName)
global alreadyFoundFunctions

functionName = lower(functionName);
[list,builtins,classes] = depfun(functionName,'-quiet','-toponly');
root = matlabroot;
alreadyFoundFunctions = {functionName};
depmodules = {};
toolboxroot = [ matlabroot filesep 'toolbox' filesep ];
for i=1:length(list)
    [pathstr, name, ext] = fileparts(list{i});
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
        [pathstr, name, ext] = fileparts(list{i});
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
