%   PAwarmup() - launch a set of empty tasks
%
%   Description:
%       used to start the Matlab engines in the infrastructure, in case they are not already running, 
%       or test the responsiveness
%
%   Usage:
%       >> PAwarmup([X , debug]);
%
%   Inputs:
%       X - number of vanilla tasks to execute
%       debug - debug mode ('-debug')
%
%   Ouputs: none
%
%/*
% * ################################################################
% *
% * ProActive: The Java(TM) library for Parallel, Distributed,
% *            Concurrent computing with Security and Mobility
% *
% * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
function varargout = PAwarmup(varargin)
if (nargin == 1)
    if isnumeric(varargin{1})
        X = varargin{1};
    elseif strcmp(varargin{1},'-debug') == 0
        debug = true;
    else 
        debug = false;
        end
elseif (nargin == 2)
        X = varargin{1};
        if strcmp(varargin{2},'-debug') == 0
            debug = true;
        else 
            debug = false;
        end
else
    X = 100;
    debug = false;
end
disp('Initializing Matlab engines. This may take a while ...')
% Creating X vanilla tasks to warm up the engine
inputScripts = javaArray('java.lang.String',X);
mainScripts = javaArray('java.lang.String',X);
for i=1:X
    inputScripts(i)=java.lang.String('in=0;');
    mainScripts(i)=java.lang.String('out=0;');
end
% Waiting for the results of these tasks
[pathstr, name, ext, versn] = fileparts(mfilename('fullpath'));
url = java.net.URL(['file:' pathstr 'checkMatlab' '.js']);
solver = PAgetsolver();
res = solver.solve(inputScripts,mainScripts, url, org.ow2.proactive.scheduler.common.job.JobPriority.NORMAL, debug);
res = org.objectweb.proactive.api.PAFuture.getFutureValue(res);
disp('Engines initialization terminated !');