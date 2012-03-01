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
function varargout = PAprepare()

% Setting the English locale, mandatory for ptolemy
java.util.Locale.setDefault(java.util.Locale.ENGLISH);

[pathstr, name, ext] = fileparts(mfilename('fullpath'));

% Scheduler root
javafile = java.io.File(pathstr);
scheduling_dir_java = javafile.getParentFile().getParentFile().getParentFile().getParentFile().getParent();
scheduling_dir = char(scheduling_dir_java.toString());

opt=PAoptions;

% Log4J file
log4jFile = java.io.File([scheduling_dir filesep 'config' filesep 'log4j' filesep 'log4j-client']);
urlLog4jFile = log4jFile.toURI().toURL();
java.lang.System.setProperty('log4j.configuration',urlLog4jFile.toExternalForm());
java.lang.System.setProperty('proactive.configuration', opt.ProActiveConfiguration);

% Dist libs
fs=filesep();
dist_lib_dir = [scheduling_dir fs 'dist' fs 'lib'];
if ~exist(dist_lib_dir,'dir')
    plugins_dir = [scheduling_dir fs 'plugins']; 
    dirdir=dir([plugins_dir fs 'org.ow2.proactive.scheduler.lib_*']);
    dd=dirdir.name;
    dist_lib_dir = [plugins_dir fs dd fs 'lib'];
    if ~exist(dist_lib_dir,'dir')
        error(['PAprepare::cannot find directory ' dist_lib_dir]);
    end
end


proactiveset = 0;
jcp = javaclasspath();
for i=1:length(jcp)
    line = jcp{i};
    if findstr(line,'ProActive_Scheduler-matsciemb.jar')
        proactiveset = 1;
    end
end



if ~proactiveset
    % script engines must be at the beginning to avoid jar index problems    
    jars = opt.EmbeddedJars;
    for i=1:length(jars)
        jarsFullPath{i} = [dist_lib_dir filesep jars{i}];
    end   
    
    warning('off')
    
    for i=1:length(jars)
        javaaddpath(jarsFullPath{i},'-END');
    end
        
    warning('on')
    
end

end

function out = shortname(in, match)
while true
    [str, in] = strtok(in);
    if isempty(str),  break;  end
    if strcmp(str, match)
        out = old_str;
    end
    old_str = str;
end
end

