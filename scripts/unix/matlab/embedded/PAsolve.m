%   PAsolve() - distribute a matlab function using parametric sweep
%
%   Usage:
%       >> results = PAsolve(args, function);
%
%   Inputs:
%       args - a one-dimensional cell array of objects holding the parameters. If the cell
%              contains X elements, then X tasks will be deployed.
%               Nested cells and structures can be used inside the cell array.
%               Functions, or other Matlab user-defined classes are not supported.
%       function - a handle to a Matlab or user-defined function
%
%   Ouputs:
%       results - a cell array containing the results. The cell array will be of the same size as the input cell array.
%
%/*
% * ################################################################
% *
% * ProActive: The Java(TM) library for Parallel, Distributed,
% *            Concurrent computing with Security and Mobility
% *
% * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
% * Contact: proactive@objectweb.org
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
% PAsolve distribute a matlab task using parametric sweep
% results = PAsolve(solver, args, func)
% solver : a connection to the scheduler already established
% args : a one-dimensional cell array of objects holding the parameters. If the cell
%       contains X elements, then X tasks will be deployed. Current limitations
%       impose that elements are numeric or character arrays only
% func : a function handle to the task that will be executed. This function
%       must have one single parameter of a type corresponding to the args
%       parameter's content, and must return one single value.
% results : a one-dimensional cell array of results.
%
% Example: results = PAsolve(solver, {1, 2, 3, 4, 5}, @factorial)
% 
function results = PAsolve(args, func)

% Checking arguments 
% if ~isa(solver, 'pa.stub.org.objectweb.proactive.extensions.scheduler.ext.matlab.embedded._StubAOMatlabEnvironment')
%     error('solver parameter should be a connection to the proactive scheduler obtained from the PAconnect function');
% end
if ~isa(args, 'cell')
    error('args parameter should be of class "cell" (a cell array of values passed to each parallel task instance)');
end
if ~isa(func, 'function_handle')
    error('func parameter should be of class "function_handle" (the function handle of the task needed to be executed in parallel)');
end
failure = '';
try 
if nargin(func)~=1
    failure = 'func parameter should be a function with one and only one input parameter';
end
catch err
    error('func parameter is a script, expected a function with one input and one output');
end
if length(failure) > 0
    error(failure);
end
if nargout(func)~=1
    error('func parameter should be a function with one and only one output parameter');
end

% Get the solver from memory
solver = PAgetsolver();

% We create the string version of the function 
strfunc = func2str(func);  
if strfunc(1) ~= '@'
    strfunc = strcat ('@', strfunc);
end

% Creating the tasks
taskList = javaArray('org.objectweb.proactive.extensions.scheduler.ext.matlab.SimpleMatlab',length(args));
input = strcat('restoredefaultpath;addpath(''',getUserPath,''');');
for i=1:length(args)
    %main = 'lasterror(''reset'');';
    
    % Creating the input command
    % (We use this amazing contribution which converts (nearly) any variable
    % to an evaluatable string)
    main = strcat('in = ',vararg2str(args(i)));

    % Creating the rest of the command (evaluation of the user function)
    main = strcat(main ,'; func = ', strfunc,'; out = func(in);');
    %main = strcat(main, 'lasterror_in_comp = lasterror; if lasterror_in_comp.identifier ~= '''' disp(lasterror) end');
    %disp(main);
    task = org.objectweb.proactive.extensions.scheduler.ext.matlab.SimpleMatlab(input,main);
    taskList(i) = task;
end

% use the selection script which figures out if matlab is installed
url = java.net.URL('http://proactive.inria.fr/userfiles/file/scripts/checkMatlab.js');
% send the task list to the scheduler
res = solver.solve(taskList,url,org.objectweb.proactive.extensions.scheduler.common.job.JobPriority.NORMAL);
% We wait for the results
res = org.objectweb.proactive.api.PAFuture.getFutureValue(res);
results = cell(1, res.size());
for i=1:res.size()
   results{i}=parse_token_output(res.get(i-1));
end