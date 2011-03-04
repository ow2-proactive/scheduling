%   PAconnect() - connects to the ProActive scheduler
%
%   Usage:
%       >> PAconnect(url);
%       >> jobs = PAconnect(url);
%
%   Example :
%
%       >> jobs = PAconnect('rmi://scheduler:1099')
%
%
%   Inputs:
%
%       url - url of the scheduler
%
%   Ouputs:
%
%       jobs - id of jobs that were not terminated at matlab's previous
%       shutdown
%
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
function jobs = PAconnect(varargin)

if nargin ~= 1 || ~ischar(varargin{1})
    error('PAconnect accepts only one argument (the url to the scheduler)');
end
url = varargin{1};

sched = PAScheduler;

% Verify that proactive is already on the path or not
p = javaclasspath('-all');
cptoadd = 1;
for i = 1:length(p)
    if (strfind(p{i}, 'ProActive.jar'))
        cptoadd = 0;
    end
end
if cptoadd == 1
    sched.PAprepare();
end

tmpsolver = sched.PAgetsolver();
if ~strcmp(class(tmpsolver), 'double')
    if tmpsolver.isLoggedIn()
        error('This session is already connected to a scheduler, only one connection can be issued at a time');
    end
    solver = tmpsolver;
else
    % Creating the connection

    solver = org.objectweb.proactive.api.PAActiveObject.newActive('org.ow2.proactive.scheduler.ext.matlab.client.AOMatlabEnvironment',[] );

    ok = solver.join(url);
    if ~ok
        error('Error while connecting');
    end
    % Recording the solver inside the session, each further call to PAgetsolver
    % will retrieve it
    sched.PAgetsolver(solver);
end


% create the frame

disp('Connection successful, please enter login/password');
loggedin = false;
msg = 'Connect to the Scheduler';
attempts = 1;
while ~loggedin && attempts <= 3
    [login,pwd]=sched.logindlg('Title',msg);
    try
        solver.login(login,pwd);
        loggedin = true;
    catch ME
        attempts = attempts+1;
        msg = ['Incorrect Login/Password, try ' num2str(attempts)];
    end
end
sched.PAgetlogin(login);

disp('Login succesful');
%PAoptions('Debug',true);
opt = PAoptions();
reconnected = false;
if exist(opt.DisconnectedModeFile,'file')
    errorreconnecting = false;
    if isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
        try
            load(opt.DisconnectedModeFile, 'registryurl');
            disp('Reconnecting to existing dataspace handler, please wait...');
            org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.init(registryurl,'MatlabInputSpace', 'MatlabOutputSpace', opt.Debug);
        catch ME
            disp('There was a problem reconnecting to previous dataspace handler.');
            if isa(ME,'MException')
                disp(getReport(ME));
            elseif isa(ME, 'java.lang.Throwable')
                ME.printStackTrace();
            end
            if exist(opt.DisconnectedModeFile,'file')
                delete(opt.DisconnectedModeFile);
            end
            errorreconnecting = true;            
        end
    end  
    if errorreconnecting
        reconnected = false;
    else
        reconnected = true;
    end
    
    if ~errorreconnecting
        try
            sched = PAScheduler;
            sched.PATaskRepository('load');
            jobs = sched.PATaskRepository('uncomplete');
            if length(jobs) > 0
                str='';
                for i=1:length(jobs)
                    str = [ str ' ' jobs{i}];
                end
                disp(['The following jobs were uncomplete before last matlab shutdown : ' str ]);
            end
        catch ME
            disp('There was a problem retrieving previous jobs.');
            disp(getReport(ME));
            if exist(opt.DisconnectedModeFile,'file')
                delete(opt.DisconnectedModeFile);
            end
        end
    end

end
if ~reconnected && isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
    disp('Creating dataspace handler, please wait...');
    org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.init([],'MatlabInputSpace', 'MatlabOutputSpace', opt.Debug);
    disp('Dataspace handler created');
end




