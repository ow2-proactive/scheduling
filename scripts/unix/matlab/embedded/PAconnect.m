%   PAconnect() - connects to the ProActive scheduler
%
%   Usage:
%       >> PAconnect(url);
%       >> PAconnect('//eon1')
%
%       >> PAconnect(proactive, url);
%       >> PAconnect('my_path_to-proactive', '//eon1')
%
%   Inputs:
%       proactive - path to the proactive installation directory,
%                   if this parameter is not specified, the proactive directory
%                   needs to be selected manually.
%       url - url of the scheduler
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
function varargout = PAconnect(varargin)
if nargin > 1
    proactive = varargin{1};
    url = varargin{2};
    if ~isa(proactive, 'char')
        error('proactive parameter should be a character array pointing to proactive path');
    end
else
    url = varargin{1};
end
if ~isa(url, 'char')
    error('url parameter should be a character array containing the scheduler url');
end

if (nargout > 0) 
    error('two many output arguments');
end

% Verify that proactive is already on the path or not
p = javaclasspath('-all');
cptoadd = 1;
for i = 1:length(p)
    if (strfind(p{i}, 'ptolemy.jar'))
        cptoadd = 0;
    end
end
if cptoadd == 1
    if nargin > 1
        PAprepare(proactive);
    else
        PAprepare();
    end
end
% Creating the connection
params = javaArray('java.lang.Object',1);
params(1) = java.lang.String(url);
solver = org.objectweb.proactive.api.PAActiveObject.newActive('org.objectweb.proactive.extensions.scheduler.ext.matlab.embedded.AOMatlabEnvironment',params );
PAgetsolver(solver);

% create the frame
loginFrame = org.objectweb.proactive.extensions.scheduler.ext.matlab.embedded.LoginFrame(solver);
% display it
loginFrame.start();
% get the button from the frame
button = loginFrame.getLoginButton();
% convert it to a special java object
button = handle(button, 'callbackproperties');
% set callback
set(button, 'ActionPerformedCallback', {@doAction});
global button_handle_global_data;
button_handle_global_data.loginFrame = loginFrame;
button_handle_global_data.solver = solver;



function doAction(srcObj, evd)
% srcObj is the button object from above
% evd is the ActionEvent object
global button_handle_global_data;
if button_handle_global_data.loginFrame.checkLogin()
    initEngines()
end


function initEngines()
% global button_handle_global_data;
% disp('Initializing Matlab engines. This may take a while ...')
% % Creating 100 vanilla tasks to warm up the engine
% taskList = javaArray('org.objectweb.proactive.extensions.scheduler.ext.matlab.SimpleMatlab',100);
% for i=1:100
%     task = org.objectweb.proactive.extensions.scheduler.ext.matlab.SimpleMatlab('in=0;','out=0;');
%     taskList(i) = task;
% end
% % Waiting for the results of these tasks
% url = java.net.URL('http://proactive.inria.fr/userfiles/file/scripts/checkMatlab.js');
% res = button_handle_global_data.solver.solve(taskList, url, org.objectweb.proactive.extensions.scheduler.common.job.JobPriority.NORMAL);
% res = org.objectweb.proactive.api.PAFuture.getFutureValue(res);
% disp('Engines initialization terminated !');
