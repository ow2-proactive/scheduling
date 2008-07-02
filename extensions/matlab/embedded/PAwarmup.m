%   PAwarmup() - launch a set of empty tasks
%
%   Description:
%       used to start the Matlab engines in the infrastructure, in case they are not already running, 
%       or test the responsiveness
%
%   Usage:
%       >> PAwarmup();
%       >> PAwarmup(100);
%
%   Inputs:
%       X - number of vanilla tasks to execute
%
%   Ouputs: none
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
function varargout = PAwarmup(varargin)
if (nargin == 1)
    X = varargin{1};
else
    X = 100;
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
url = java.net.URL('http://proactive.inria.fr/userfiles/file/scripts/checkMatlab.js');
solver = PAgetsolver();
res = solver.solve(inputScripts,mainScripts, url, org.ow2.proactive.scheduler.common.job.JobPriority.NORMAL);
res = org.objectweb.proactive.api.PAFuture.getFutureValue(res);
disp('Engines initialization terminated !');