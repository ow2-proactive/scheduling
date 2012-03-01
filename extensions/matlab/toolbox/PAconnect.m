function jobs = PAconnect(url, credpath)
% PAconnect connects to the ProActive scheduler
%
% Syntax
%
%       PAconnect(url [,credpath]);
%       jobs = PAconnect(url [,credpath]);
%
% Inputs
%
%       url - url of the scheduler
%       credpath - path to the login credential file
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
%       You can as well encrypt credentials using the command line tool
%       "create-cred" to automate the connection.
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

sched = PAScheduler;
opt = PAoptions();

% Verify that proactive is already on the path or not
p = javaclasspath('-all');
cptoadd = 1;
for i = 1:length(p)
    if (strfind(p{i}, 'ProActive_Scheduler-matlabemb.jar'))
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
    isJVMdeployed = 1;
    isConnected = 0;
    try
        isConnected = tmpsolver.isConnected();
    catch
        isJVMdeployed = 0;
    end
else 
    isJVMdeployed = 0;
    isConnected = 0;
end


if ~isJVMdeployed
    % Creating a new connection
    deployJVM(sched,opt);
end
if ~isConnected
    % joining the scheduler
    solver = sched.PAgetsolver();
    ok = solver.join(url);
    if ~ok
        error('PAconnect::Error while connecting');
    end
    dataspaces(sched, opt);
else
    solver = tmpsolver;
end

if solver.isLoggedIn()
    error('This session is already connected to a scheduler.');
end    

if exist('credpath', 'var')
    login(solver,sched, credpath);
else
    login(solver, sched);
end



end

function deployJVM(sched,opt)
deployer = org.ow2.proactive.scheduler.ext.matsci.client.embedded.util.StandardJVMSpawnHelper.getInstance();
home = getenv('JAVA_HOME');
fs=filesep();
if length(home) > 0
    deployer.setJavaPath([home fs 'bin' fs 'java']);
end
[pathstr, name, ext] = fileparts(mfilename('fullpath'));
javafile = java.io.File(pathstr);
scheduling_dir = char(javafile.getParentFile().getParentFile().getParent().toString());

dist_lib_dir = [scheduling_dir fs 'dist' fs 'lib'];
if ~exist(dist_lib_dir,'dir')
    plugins_dir = [scheduling_dir fs 'plugins'];
    dirdir=dir([plugins_dir fs 'org.ow2.proactive.scheduler.lib_*']);
    dd=dirdir.name;
    dist_lib_dir = [plugins_dir fs dd fs 'lib'];
    if ~exist(dist_lib_dir,'dir')
        error(['PAconnect::cannot find directory ' dist_lib_dir]);
    end
end
jars = opt.ProActiveJars;
jarsjava = javaArray('java.lang.String', length(jars));
for i=1:length(jars)
    jarsjava(i) = java.lang.String([dist_lib_dir filesep jars{i}]);
end
deployer.setDebug(opt.Debug);
deployer.setClasspathEntries(jarsjava);
deployer.setProActiveConfiguration(opt.ProActiveConfiguration);
deployer.setLog4JFile(opt.Log4JConfiguration);
deployer.setPolicyFile(opt.SecurityFile);
deployer.setClassName('org.ow2.proactive.scheduler.ext.matsci.middleman.MiddlemanDeployer');

if exist(opt.DisconnectedModeFile,'file')
    load(opt.DisconnectedModeFile, 'rmiport');
else
    rmiport = opt.RmiPort;
end
deployer.setRmiPort(rmiport);

pair = deployer.deployOrLookup();
itfs = pair.getX();
PAoptions('RmiPort',pair.getY());
solver = deployer.getMatlabEnvironment();
sched.PAgetsolver(solver);
registry = deployer.getRegistry();
sched.PAgetDataspaceRegistry(registry);
jvmint = deployer.getJvmInterface();
sched.PAgetJVMInterface(jvmint);

end

function login(solver, sched, credpath)
% Logging in
if exist('credpath', 'var')
    try
        solver.login(credpath);
    catch ME
        disp(getReport(ME));
        error('PAconnect::Authentication error');
    end
else
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

end
disp('Login succesful');

end

function dataspaces(sched,opt)
% Dataspace Handler
registry = sched.PAgetDataspaceRegistry();
registry.init('MatlabInputSpace', 'MatlabOutputSpace', opt.Debug);
if exist(opt.DisconnectedModeFile,'file')
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
end
end




