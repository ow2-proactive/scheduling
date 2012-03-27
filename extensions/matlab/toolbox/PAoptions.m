% PAoptions sets or returns the current options for the PAsolve execution
%
% Syntax
%
%       >> options = PAoptions();
%       >> PAoptions(param,value, ...);
%
% Inputs
%
%       param - a string containing the parameter name
%       value - the new value of this parameter
%
% Outputs
%
%       options - a structure with fields corresponding to parameters
%       names, containing all options values.
%
% Description
%       
%       PAoptions sets options used by the next PAsolve call. A structure
%       containing the current options can be retrieved by calling
%       PAoptions with no parameter.
%
% Example
%
%       >> PAoptions('Debug', true);
%       >> r = PAsolve(@factorial, 1, 2, 3, 4)  % Runs PAsolve in "Debug"
%       mode.
%
% Parameters
%
%   JobName
%               Name of the job that will be submitted to the Scheduler
%
%   JobDescription
%               Description of the job that will be submitted to the scheduler
%
%   Debug               true | false | 'on' | 'off'
%
%               Debug mode, default to 'off'
%
%
%   TransferEnv       true | false | 'on' | 'off'
%
%               Transfers the environment in which the PAsolve/PAeval function is called
%               to every remote tasks. Variables transferred this way need to be accessed inside the submitted function
%               via the evalin('caller', ...) syntax. Global variables are also transferred and can be accessed normally via the "global" keyword
%               default to 'off'
%   EnvExcludeList       
%               Comma separated list of variables which should be
%               excluded from the workspace when transferring the
%               environment (TransferEnv)
%   EnvExcludeTypeList
%               Comma separated list of object types which should be
%               excluded from the workspace when transferring the
%               environment (TransferEnv)
%   AutomaticDump     true | false | 'on' | 'off'
%               If this option is set to true, the current state of the
%               PAsolve job database will be systematically saved at each
%               PAsolve call. At a price of a slight slowing down of PAsolve
%               calls, it allows to keep the database information in case
%               the local Matlab session crashes. Thus, after restarting
%               Matlab, all the previous session jobs results can be
%               retrieved via PAgetResults.
%
%   CustomDataspaceURL        char
%               URL of the dataspace (both input and output) to expose, if
%               you don't want to rely on ProActive's automatic transfer
%               protocol. The dataspace server must of course be started
%               and configure manually in that case. e.g
%               ftp://myserver/rootpath
%
%   CustomDataspacePath       char
%               Path to the root of the Custom Dataspace provided by
%               CustomDataspaceURL. For example if ftp://myserver/rootpath
%               is the URL of the Dataspace and the rootpath directory
%               corresponds on the file system to /user/myserver/.../root
%               then this path must be specified in CustomDataspacePath.
%
%
%   TransferMatFileOptions    char
%               If TranferEnv or TransferVariables is set to on, tells which options are used to save the local environment
%               See the "save" command for more information. Default to
%               '-v7'
%
%   Fork        true | false | 'on' | 'off'
%               Runs the tasks in a separate JVM process
%
%   RunAsMe     true | false | 'on' | 'off'
%               Runs the tasks under the account of the current user, default to 'off'
%
%   RemoveJobAfterRetrieve     true | false | 'on' | 'off'
%               Removes the job automatically after all results have been
%               retrieved. If the options is "off", the job is removed at the end of the
%               matlab session, or manually via PAjobRemove. default to
%               'on'
%
%   LicenceServerURL  char
%               URL of the FlexNet proxy server. If empty, no license check will be done
%
%   VersionPref       char
%               Determines the matlab version preferred to use by the worker, e.g. 7.5
%
%   VersionRej        char
%               A string containing a list of matlab versions that must not be used, delimiters can be spaces or commas : 7.5, 7.7
%
%   VersionMin        char
%               A minimum matlab version that can be used
%
%   VersionMax        char
%               A maximum matlab version that can be used
%
%   Priority          'Idle' | 'Lowest' | 'Low' | 'Normal' | 'High' | 'Highest'
%               Priority used by default for jobs submitted with PAsolve,
%               default to 'Normal'
%
%   CustomScript
%               url or path of a user-defined selection script used in
%               addition to (before) FindMatlabScript and MatlabReservationScript
%
%   FindMatlabScript
%               url or path of selection script used to find matlab
%               (internal)
%
%   MatlabReservationScript
%               url or path of selection script used to reserve matlab
%               tokens (internal)
%
%   ProActiveJars and EmbeddedJars
%               Comma separated list of jar files used by ProActive
%               (internal)
%
%   ProActiveConfiguration
%               path to ProActive configuration file (internal)
%
%   Log4JConfiguration
%               path to log4j configuration file (internal)
%
%   SecurityFile
%               path to java security configuration file (internal)
%
%   RmiPort
%               default RMI port used when deploying the middleman JVM (internal)
%
%   JvmTimeout
%               default timeout used when deploying the middleman JVM (internal)
%
%   DisconnectedModeFile
%               path to disconnected mode temporary file (internal)
%
%   UseMatlabControl
%               Internal : do we use the MatlabControl framework
%
%   CleanAllTempFilesDirectly
%               do we clean the temporary files after all results of a PAsolve 
%               call are received (default), or do we wait until Matlab terminates 
%               to allow PAgetResults calls for this job. (internal)
%

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
function opts = PAoptions(varargin)

mlock
persistent pa_options

if  nargin == 0 && exist('pa_options','var') == 1 && ~isempty(pa_options)
    opts = pa_options;
    return;
elseif mod(nargin,2) ~= 0
    error(['Wrong number of arguments : ' num2str(nargin)]);
end

logcheck = @(x)(ischar(x) && ismember(x, {'on','off', 'true', 'false'})) || islogical(x) || (isnumeric(x) && ((x == 0)||(x == 1)));
versioncheck = @(x)((isnumeric(x)&&isempty(x)) || (ischar(x) && isempty(x)) || (ischar(x) && ~isempty(regexp(x, '^[1-9][\d]*\.[\d]+$'))));
versionlistcheck = @(x)((isnumeric(x)&&isempty(x)) || (ischar(x) && isempty(x)) || (ischar(x) &&  ~isempty(regexp(x, '^([1-9][\d]*\.[\d]+[ ;,]+)*[1-9][\d]*\.[\d]+$'))));

jarlistcheck = @(x)(ischar(x) &&  ~isempty(regexp(x, '^([\w\-]+\.jar[ ;,]+)*[\w\-]+\.jar$')));
listcheck = @(x)(ischar(x) && (isempty(x) || ~isempty(regexp(x, '^([^ ;,]+[ ;,]+)*[^ ;,]+$'))));
listtrans = @listtocell;

urlcheck=@(x)((isnumeric(x)&&isempty(x)) || ischar(x));

charornull = @(x)((isnumeric(x)&&isempty(x)) || ischar(x));

charornum = @(x)(isnumeric(x) || ischar(x));


v = version;
[maj,v] = strtok(v,'.');
[min,v] = strtok(v,'.');



[pathstr, name, ext] = fileparts(mfilename('fullpath'));
javafile = java.io.File(pathstr);
scheduling_dir = char(javafile.getParentFile().getParentFile().getParent().toString());
tmp_dir = char(java.lang.System.getProperty('java.io.tmpdir'));
home_dir = char(java.lang.System.getProperty('user.home'));
logtrans = @(x)((islogical(x) && x) || (ischar(x) && (strcmp(x,'on') || strcmp(x,'true'))) || (isnumeric(x)&&(x==1)));
variabletrans = @(x)(strrep(strrep(strrep(x, '$SCHEDULER$', scheduling_dir),'$TMP$',tmp_dir), '$HOME$', home_dir));
scripttrans = @(x)(['file:' strrep(variabletrans(x), '\', '/')]);
conftrans = @(x)(strrep(variabletrans(x),'/',filesep));

ischarornull = @(x)(ischar(x) || isnumeric(x)&&isempty(x));

id = @(x)x;


j=1;
inputs(j).name = 'JobName';
inputs(j).default = 'Matlab';
inputs(j).check = @ischar;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'JobDescription';
inputs(j).default = 'Set of parallel Matlab tasks';
inputs(j).check = @ischar;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'Debug';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'TimeStamp';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'LicenseServerURL';
inputs(j).default = [];
inputs(j).check = urlcheck;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'Fork';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'RunAsMe';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'RemoveJobAfterRetrieve';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'TransferEnv';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'EnvExcludeList';
inputs(j).default = '';
inputs(j).check = listcheck;
inputs(j).trans = listtrans;
j=j+1;
inputs(j).name = 'EnvExcludeTypeList';
inputs(j).default = '';
inputs(j).check = listcheck;
inputs(j).trans = listtrans;
j=j+1;
inputs(j).name = 'AutomaticDump';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'CustomDataspaceURL';
inputs(j).default = [];
inputs(j).check = urlcheck;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'CustomDataspacePath';
inputs(j).default = [];
inputs(j).check = charornull;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'TransferMatFileOptions';
inputs(j).default = '-v7';
inputs(j).check = charornull;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'VersionPref';
inputs(j).default = [maj '.' min];
inputs(j).check = versioncheck;
inputs(j).trans = @versiontrans;
j=j+1;
inputs(j).name = 'VersionRej';
inputs(j).default = [];
inputs(j).check = versionlistcheck;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'VersionMin';
inputs(j).default = [];
inputs(j).check = versioncheck;
inputs(j).trans = @versiontrans;
j=j+1;
inputs(j).name = 'VersionMax';
inputs(j).default = [];
inputs(j).check = versioncheck;
inputs(j).trans = @versiontrans;
j=j+1;
inputs(j).name = 'MatlabReservationScript';
inputs(j).default = ['$SCHEDULER$' filesep 'extensions' filesep 'matlab' filesep 'script' filesep 'reserve_matlab.rb' ];
inputs(j).check = @ischar;
inputs(j).trans = scripttrans;
j=j+1;
inputs(j).name = 'FindMatlabScript';
inputs(j).default = ['$SCHEDULER$' filesep 'extensions' filesep 'matlab' filesep 'script' filesep 'file_matlab_finder.rb' ];
inputs(j).check = @ischar;
inputs(j).trans = scripttrans;
j=j+1;
inputs(j).name = 'CustomScript';
inputs(j).default = [];
inputs(j).check = ischarornull;
inputs(j).trans = scripttrans;
j=j+1;
inputs(j).name = 'Priority';
inputs(j).default = 'Normal';
inputs(j).check = @(x)(ischar(x) && ismember(x, {'Idle', 'Lowest', 'Low', 'Normal', 'High', 'Highest'}));
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'ZipInputFiles';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'ZipOutputFiles';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'WindowsStartupOptions';
inputs(j).default = '-automation -nodesktop -nosplash -nodisplay';
inputs(j).check = @ischar;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'LinuxStartupOptions';
inputs(j).default = '-nodesktop -nosplash -nodisplay';
inputs(j).check = @ischar;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'ProActiveJars';
inputs(j).default = 'jruby.jar,jruby-engine.jar,jython.jar,jython-engine.jar,ProActive.jar,ProActive_Scheduler-client.jar,ProActive_SRM-common-client.jar,ProActive_Scheduler-matsci.jar';
inputs(j).check = jarlistcheck;
inputs(j).trans = listtrans;
j=j+1;
inputs(j).name = 'EmbeddedJars';
inputs(j).default = 'ProActive_Scheduler-matsciemb.jar';
inputs(j).check = jarlistcheck;
inputs(j).trans = listtrans;
j=j+1;
inputs(j).name = 'ProActiveConfiguration';
inputs(j).default = ['$SCHEDULER$' filesep 'config' filesep 'proactive' filesep 'ProActiveConfiguration.xml'];
inputs(j).check = @ischar;
inputs(j).trans = conftrans;
j=j+1;
inputs(j).name = 'Log4JConfiguration';
inputs(j).default = ['$SCHEDULER$' filesep 'config' filesep 'log4j' filesep 'log4j-client'];
inputs(j).check = @ischar;
inputs(j).trans = conftrans;
j=j+1;
inputs(j).name = 'SecurityFile';
inputs(j).default = ['$SCHEDULER$' filesep 'config' filesep 'security.java.policy-client'];
inputs(j).check = @ischar;
inputs(j).trans = conftrans;
j=j+1;
inputs(j).name = 'DisconnectedModeFile';
inputs(j).default = ['$HOME$' filesep '.PAsolveTmp.mat'];
inputs(j).check = @ischar;
inputs(j).trans = conftrans;
j=j+1;
inputs(j).name = 'UseMatlabControl';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'CleanAllTempFilesDirectly';
inputs(j).default = true;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'SchedulingDir';
inputs(j).default = scheduling_dir;
inputs(j).check = @ischar;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'RmiPort';
inputs(j).default = 1111;
inputs(j).check = charornum;
inputs(j).trans = @charornumtrans;
j=j+1;
inputs(j).name = 'JvmTimeout';
inputs(j).default = 1200;
inputs(j).check = charornum;
inputs(j).trans = @charornumtrans;




% Parsing option file
if ~exist('pa_options','var') == 1 || ~isstruct(pa_options)
    userdir = char(java.lang.System.getProperty('user.home'));
    optionpath = [userdir filesep '.matlab' filesep 'PAoptions.ini'];
    if exist(optionpath, 'file');
        fid = fopen(optionpath, 'r');
    else
        optionpath = [scheduling_dir filesep 'extensions' filesep 'matlab' filesep 'config', filesep, 'toolbox', filesep, 'PAoptions.ini'];
        fid = fopen(optionpath, 'r');
    end
    try
        C = textscan(fid, '%s = %[^\n]', 'CommentStyle', '%');
        for i=1:length(C{1})
            for j=1:length(inputs)
                if strcmp(C{1}{i},inputs(j).name)
                    chk = inputs(j).check;
                    tf = chk(C{2}{i});
                    if ~tf
                        error(['Parse error when loading option file ' optionpath ', option ' C{1}{i} ' doesn''t satisfy check ' func2str(chk) ]);
                    end
                    trans = inputs(j).trans;
                    pa_options = setfield(pa_options, inputs(j).name, trans(C{2}{i}));                    
                end
            end
        end
    catch ME
        fclose(fid);
        throw(ME);
    end
    fclose(fid);
end


for i = 1:length(inputs)
    default = true;
    trans = inputs(i).trans;
    Parameter = inputs(i).default;
    for j= 1:nargin/2
        optionName = varargin{2*(j-1)+1};
        value = varargin{2*j};

        if strcmp(inputs(i).name, optionName)
            chk = inputs(i).check;
            tf = chk(value);
            if ~tf
                error(['Argument ' optionName ' doesn''t satisfy check ' func2str(chk) ]);
            end
            default = false;
            Parameter = trans(value);
        end
    end
    if ~default || ~(isstruct(pa_options) && isfield(pa_options,inputs(i).name))
        pa_options = setfield(pa_options, inputs(i).name, Parameter);
    end
end

opts = pa_options;

end

function num = charornumtrans(x)
if ischar(x)
    num = str2num(x);
else
    num = x;
end
end

function v = versiontrans(x)
if ischar(x) && isempty(x)
    v = [];
else
    v = x;
end
end

function cl=listtocell(x)
cl={};
i=1;
remain=x;
if iscell(x)
    cl = x;
    return;
end
goon=true;
while goon
    [str, remain] = strtok(remain, ',; ');
    if isempty(str) || length(str) == 0
        goon=false;
    else
        cl{i}=str;
        i=i+1;
    end
end
end

