%   PAoptions() - sets or returns the current options for the PAsolve/PAeval execution
%
%   Usage:
%       >> options = PAoptions();
%       >> PAoptions(param,value, ...);
%
%   Parameters:
%       
%   Debug               true | false | 'on' | 'off'      
%   
%               Debug mode, default to 'off'
%
%   TimeStamp           true | false | 'on' | 'off'      
%   
%               TimeStamp mode in outputs, default to 'off'
%
%   TransferSource       true | false | 'on' | 'off'      
%               
%               Transfers source code used by the called function (all
%               dependant matlab user functions to remote matlab engine,
%               default to 'on'
%
%   TransferEnv       true | false | 'on' | 'off'      
%               
%               Transfers the environment in which the PAsolve/PAeval function is called
%               to every remote calls. If variables from this "caller"
%               environment need to be accessed inside the batch function,
%               they should be done using the evalin('caller', ...) syntax
%               default to 'off'
%   TransferVariables       true | false | 'on' | 'off'      
%               
%               Transfers the input and return parameters of matlab
%               functions as a file rather than using the Ptolemy Matlab/Java interface
%               default to 'on' as their might be rounding errors betwen
%               matlab and java
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
%
%   KeepEngine       true | false | 'on' | 'off'  
%               Determines wether the remote matlab engines should be
%               destroyed after each task. 'on' provides a faster response,
%               but keeps matlab token in use, therefore 'off' is the default behavior.
%               Additionally on linux system, clearing variables doesn't
%               remove memory usage by the matlab process. KeepEngine set
%               to 'on' can though lead to OutOfMemory errors.
%            
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
%   ProActiveJars
%               Comma separated list of jar files used by ProActive
%               (internal)
%
%   ProActiveConfiguration
%               path to ProActive configuration file (internal)
%
%   DisconnectedModeFile
%               path to disconnected mode temporary file (internal)
%
%
%
%/*
% * ################################################################
% *
% * ProActive: The Java(TM) library for Parallel, Distributed,
% *            Concurrent computing with Security and Mobility
% *
% * Copyright (C) 1997-2010 INRIA/University of Nice-Sophia Antipolis
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
versioncheck = @(x)((isnumeric(x)&&isempty(x)) || (ischar(x) && ~isempty(regexp(x, '^[1-9][\d]*\.[\d]+$'))));
versionlistcheck = @(x)((isnumeric(x)&&isempty(x)) || (ischar(x) &&  ~isempty(regexp(x, '^([1-9][\d]*\.[\d]+[ ;,]+)*[1-9][\d]*\.[\d]+$'))));

jarlistcheck = @(x)(ischar(x) &&  ~isempty(regexp(x, '^([\w\-]+\.jar[ ;,]+)*[\w\-]+\.jar$')));
jarlisttrans = @jarlisttocell;

urlcheck=@(x)((isnumeric(x)&&isempty(x)) || ischar(x));

charornull = @(x)((isnumeric(x)&&isempty(x)) || ischar(x));


v = version;
[maj,v] = strtok(v,'.');
[min,v] = strtok(v,'.');


% Parsing option file
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
inputs(j).name = 'TransferSource';
inputs(j).default = true;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'KeepEngine';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'TransferEnv';
inputs(j).default = false;
inputs(j).check = logcheck;
inputs(j).trans = logtrans;
j=j+1;
inputs(j).name = 'TransferVariables';
inputs(j).default = true;
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
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'VersionRej';
inputs(j).default = [];
inputs(j).check = versionlistcheck;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'VersionMin';
inputs(j).default = [];
inputs(j).check = versioncheck;
inputs(j).trans = id;
j=j+1;
inputs(j).name = 'VersionMax';
inputs(j).default = [];
inputs(j).check = versioncheck;
inputs(j).trans = id;
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
inputs(j).name = 'ProActiveJars';
inputs(j).default = 'jruby.jar,jruby-engine.jar,jython.jar,jython-engine.jar,ProActive.jar,ProActive_Scheduler-client.jar,ProActive_SRM-common-client.jar,ProActive_Scheduler-matsci.jar';
inputs(j).check = jarlistcheck;
inputs(j).trans = jarlisttrans;
j=j+1;
inputs(j).name = 'ProActiveConfiguration';
inputs(j).default = ['$SCHEDULER$' filesep 'config' filesep 'proactive' filesep 'ProActiveConfiguration.xml'];
inputs(j).check = @ischar;
inputs(j).trans = conftrans;
j=j+1;
inputs(j).name = 'DisconnectedModeFile';
inputs(j).default = ['$HOME$' filesep '.PAsolveTmp.mat'];
inputs(j).check = @ischar;
inputs(j).trans = conftrans;


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
            inputs(j).default = trans(C{2}{i});
        end
    end
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
    if (~default || ~isfield(pa_options,inputs(i).name))       
        pa_options = setfield(pa_options, inputs(i).name, Parameter);
    end
end

opts = pa_options;
fclose(fid);
catch ME
    fclose(fid);
    throw(ME);
end

end

function cl=jarlisttocell(x)
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

