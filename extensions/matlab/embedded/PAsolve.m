%   PAsolve() - distribute a matlab function using parametric sweep
%
%   Usage:
%       >> results = PAsolve(function, args [, debug]);
%
%   Inputs:
%       func - a handle to a Matlab or user-defined function
%       args - a one-dimensional cell array of objects holding the parameters. If the cell
%              contains X elements, then X tasks will be deployed.
%              Nested cells and structures can be used inside the cell array.
%              Functions, or other Matlab user-defined classes are not supported.
%       debug - '-debug' if the computation needs to be run in debug mode
%
%   Ouputs:
%       results - a cell array containing the results. The cell array will be of the same size as the input cell array.
%
%   Example: results = PAsolve(@factorial,{1, 2, 3, 4, 5})
%
%/*
% * ################################################################
% *
% * ProActive: The Java(TM) library for Parallel, Distributed,
% *            Concurrent computing with Security and Mobility
% *
% * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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

if (nargin <2) | (nargin > 3) 
    error(['wrong number of arguments: ' int2str(nargin)]);
end

func = varargin{1};
args = varargin{2};
if (nargin == 3) && (strcmp(varargin{3},'-debug') == 1 || strcmp(varargin{3},'-d') == 1)
    debug = true;
else
    debug = false;
end
    

% Checking arguments 
% if ~isa(solver, 'pa.stub.org.ow2.proactive.scheduler.ext.matlab.embedded._StubAOMatlabEnvironment')
%     error('solver parameter should be a connection to the proactive scheduler obtained from the PAconnect function');
% end
if ~isa(args, 'cell')
    error('args parameter should be of class "cell" (a cell array of values passed to each parallel task instance)');
end
if length(args) == 0
    error('argument list of size > 0 expected');
end
if ~isa(func, 'function_handle')
    error('func parameter should be of class "function_handle" (the function handle of the task needed to be executed in parallel)');
end
failure = '';
v=version;
if str2num(v(1:3)) > 7.2
    try
    if nargin(func)~=1
        failure = 'func parameter should be a function with one and only one input parameter';
    end
    catch err
        if strcmp(err.identifier,'MATLAB:nargin:isScript') == 1
            error('func parameter is a script, expected a function with one input and one output');
        else
            throw(err);
        end
    end
else
    if nargin(func)~=1
        failure = 'func parameter should be a function with one and only one input parameter';
    end
end
if length(failure) > 0
    error(failure);
end
if nargout(func)~=1
    error('func parameter should be a function with one and only one output parameter');
end

% Get the solver from memory
solver = PAgetsolver();
if strcmp(class(solver),'double')
    error('connexion to the scheduler is not established');
end

% We create the string version of the function 
strfunc = func2str(func);  
if strfunc(1) ~= '@'
    strfunc = strcat ('@', strfunc);
end

% Creating the tasks
inputScripts = javaArray('java.lang.String',length(args));
mainScripts = javaArray('java.lang.String',length(args));
input = strcat('restoredefaultpath;addpath(''',getUserPath,''');');
for i=1:length(args)
    
    % Creating the input command
    % (We use this amazing contribution which converts (nearly) any variable
    % to an evaluatable string)
    main = strcat('in = ',vararg2str(args(i)),';');

    % Creating the rest of the command (evaluation of the user function)
    main = strcat(main ,'; func = ', strfunc,'; out = func(in);');
    inputScripts(i) = java.lang.String(input);
    mainScripts(i) = java.lang.String(main);
end

% use the selection script which figures out if matlab is installed
[pathstr, name, ext, versn] = fileparts(mfilename('fullpath'));
scriptUrl = java.net.URL(['file:' pathstr filesep 'checkMatlab' '.js']);
% find the list of toolboxes used by the user function and give it as parameter to the script
tblist = findUsedToolboxes(func2str(func));
scriptParams = javaArray('java.lang.String',length(args));
for i=1:length(tblist)
    scriptParams(i) = java.lang.String(tblist{i});    
end

% send the task list to the scheduler
resfuture = solver.solve(inputScripts,mainScripts,scriptUrl,scriptParams,org.ow2.proactive.scheduler.common.job.JobPriority.NORMAL, debug);
if length(args) == 1
  results = PAResult(resfuture);
else
  results = PAResultList(resfuture);
end
