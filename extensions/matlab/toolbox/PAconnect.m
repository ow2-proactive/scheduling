%   PAconnect() - connects to the ProActive scheduler
%
%   Usage:
%       >> res = PAconnect(url);
%       >> res = PAconnect('//eon1')
%
%       >> res = PAconnect(proactive, url);
%       >> res = PAconnect('my_path_to-proactive', '//eon1')
%
%   Inputs:
%       proactive - path to the proactive installation directory,
%                   if this parameter is not specified, the proactive directory
%                   needs to be selected manually.
%       url - url of the scheduler
%       res - cell array which is not empty when jobs were submitted before the previous Matlab session closed
%             and results were not retrieved then. The array will contain the results not retrieved before. 
% 
%   Ouputs: none
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
function oldres = PAconnect(varargin)

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


% Verify that proactive is already on the path or not
p = javaclasspath('-all');
cptoadd = 1;
for i = 1:length(p)
    if (strfind(p{i}, 'ProActive.jar'))
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

tmpsolver = PAgetsolver();
if ~strcmp(class(tmpsolver), 'double')
    if tmpsolver.isConnected() 
        error('This session is already connected to a scheduler, only one connection can be issued at a time');
    end
    solver = tmpsolver;
else
    % Creating the connection    
   
    solver = org.objectweb.proactive.api.PAActiveObject.newActive('org.ow2.proactive.scheduler.ext.matlab.client.AOMatlabEnvironment',[] );
    
   
    % Recording the solver inside the session, each further call to PAgetsolver
    % will retrieve it
    PAgetsolver(solver);
end


ok = solver.join(url)
if ~ok
    error('Error while connecting');
end

% create the frame
loginFrame = org.ow2.proactive.scheduler.ext.matlab.client.LoginFrame(solver);
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
button_handle_global_data.ok = false;
disp('Connection successful, please enter login/password');
while ~button_handle_global_data.ok
    pause(0.1);
end

% listresults = solver.getPreviousJobResults();
% oldres = cell(1,listresults.size());
% for i=0:(listresults.size()-1)
%     futureornot = listresults.get(i);
%     oldres{i+1} = PAResultList(futureornot);
% end


function doAction(srcObj, evd)
% srcObj is the button object from above
% evd is the ActionEvent object
global button_handle_global_data;
if button_handle_global_data.loginFrame.checkLogin()
   disp('Connected'); 
   button_handle_global_data.ok = true;
end



