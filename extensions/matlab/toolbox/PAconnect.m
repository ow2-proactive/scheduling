function jobs = PAconnect(varargin)
% PAconnect connects to the ProActive scheduler
%
% Syntax
%
%       PAconnect(url);
%       jobs = PAconnect(url);
%
% Inputs
%
%       url - url of the scheduler
%
% Ouputs
%
%       jobs - id of jobs that were not terminated at matlab's previous
%       shutdown
%
% Description
%
%       PAconnect connects to a running ProActive Scheduler by specifying its
%       url. If the scheduler could be reached a popup window will appear, asking
%       for login and password. ProActive Scheduler features a full account 
%       management facility along with the possibility to synchronize to existing 
%       Windows or Linux accounts via LDAP. 
%       More information can be found inside Scheduler's manual chapter "Configure 
%       users authentication". If you haven't configured any account in the 
%       scheduler use, the default account login "demo", password "demo".
%
%       PAconnect can also return the ids of PAsolve jobs that
%       were still running at the end of the last Matlab session (Disconnected
%       Mode). Results of these jobs can then be retrieved using
%       PAgetResults.
%
% Example
%
%   jobs = PAconnect('rmi://scheduler:1099')
%
% See also
%   PAsolve, PAgetResults
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
if nargin ~= 1 || ~ischar(varargin{1})
    error('PAconnect::PAconnect accepts only one argument (the url to the scheduler)');
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
reconnected = false;

% Test that the session is not already connected to a Scheduler, or that
% the connection to it is failing
tmpsolver = sched.PAgetsolver();
if ~strcmp(class(tmpsolver), 'double')
    tst = false;
    try 
        tst = tmpsolver.isConnected();
    catch ME
        % Renewing a broken connection        
        tmpsolver.terminate();
        pause(1);
        node=sched.PAgetNode();        
        nodei = node.getNodeInformation();
        nodeurl = nodei.getURL();
        try
            org.objectweb.proactive.core.node.NodeFactory.killNode(nodeurl);
        catch
        end
        node=org.objectweb.proactive.core.node.NodeFactory.createLocalNode('MatlabNode', true, [], []);
        sched.PAgetNode(node);        
        tmpsolver = org.objectweb.proactive.api.PAActiveObject.newActive('org.ow2.proactive.scheduler.ext.matlab.client.AOMatlabEnvironment',[],node );
        
        sched.PAgetsolver(tmpsolver);
        ok = tmpsolver.join(url);
        if ~ok
            error('PAconnect::Error while connecting');
        end                           
    end    
    if tst && tmpsolver.isLoggedIn()
        error('This session is already connected to a scheduler.');
    end
    solver = tmpsolver;
else
    % Creating a new connection
    node=org.objectweb.proactive.core.node.NodeFactory.createLocalNode('MatlabNode', true, [], []);
    sched.PAgetNode(node);
    solver = org.objectweb.proactive.api.PAActiveObject.newActive('org.ow2.proactive.scheduler.ext.matlab.client.AOMatlabEnvironment',[], node );

    ok = solver.join(url);
    if ~ok
        error('PAconnect::Error while connecting');
    end
    % Recording the solver inside the session, each further call to PAgetsolver
    % will retrieve it
    sched.PAgetsolver(solver);
end


% Logging in
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
        disp(getReport(ME));
        attempts = attempts+1;
        msg = ['Incorrect Login/Password, try ' num2str(attempts)];
    end
end
if attempts > 3
    error('PAconnect::Authentication error');
end
sched.PAgetlogin(login);

disp('Login succesful');

% Dataspace Handler
opt = PAoptions();
dsconnected = org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.isConnected();
if ~dsconnected && isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL) && exist(opt.DisconnectedModeFile,'file')
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
            errorreconnecting = true;            
        end
    end      
    
    % We load the job database, wether there was a problem with
    % reconnecting to the Dataspace handler or not
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
    
    if errorreconnecting
        dsconnected = false;    
    else 
        dsconnected = true;
    end

end
if ~dsconnected && isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
    disp('Creating dataspace handler, please wait...');
    org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.init([],'MatlabInputSpace', 'MatlabOutputSpace', opt.Debug);
    disp('Dataspace handler created');
end




