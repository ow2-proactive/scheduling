%   PAprepare() - prepares classpath for ProActive
%
%   Usage:
%       >> PAprepare(proactive);  // prepares ProActive for Matlab using the given proactive path
%       >> PAprepare();           // prepares ProActive for Matlab by downloading
%                                 // ProActive from the proactive website
%
%   Inputs:
%       proactive - local path to proactive
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
function varargout = PAprepare()

% Setting the English locale, mandatory for ptolemy
java.util.Locale.setDefault(java.util.Locale.ENGLISH);

[pathstr, name, ext] = fileparts(mfilename('fullpath'));

% Scheduler root
javafile = java.io.File(pathstr);
scheduling_dir_java = javafile.getParentFile().getParentFile().getParent();
scheduling_dir = char(scheduling_dir_java.toString());

opt=PAoptions;

% Log4J file
log4jFile = java.io.File([scheduling_dir filesep 'config' filesep 'log4j' filesep 'log4j-client']);
urlLog4jFile = log4jFile.toURI().toURL();
java.lang.System.setProperty('log4j.configuration',urlLog4jFile.toExternalForm());
java.lang.System.setProperty('proactive.configuration', opt.ProActiveConfiguration);

% Policy
java.lang.System.setProperty('java.security.policy',[scheduling_dir filesep 'config' filesep 'scheduler.java.policy']);

% Dist libs
dist_lib_dir = [scheduling_dir filesep 'dist' filesep 'lib'];


proactiveset = 0;
jcp = javaclasspath();
for i=1:length(jcp)
    line = jcp{i};
    if findstr(line,'ProActive.jar')
        proactiveset = 1;
    end
end



if ~proactiveset
    % script engines must be at the beginning to avoid jar index problems    
    jars = opt.ProActiveJars;
    for i=1:length(jars)
        jarsFullPath{i} = [dist_lib_dir filesep jars{i}];
    end   
    
    % ensure that the correct class loader is used inside proactive
    
    th = java.lang.Thread.currentThread();
    cl = th.getContextClassLoader();
    ucz = cl.getClass().getSuperclass();
    ucpf = ucz.getDeclaredField('ucp');
    ucpf.setAccessible(true);
    ucp = ucpf.get(cl);
    urls = ucp.getURLs();
    stack = java.util.Stack();
    for i=1:urls.length
        %if any(strfind(char(urls(i).toString()), 'jini'))
        %else
        stack.push(urls(i));
        %end
    end
    for i=1:length(jars)
        url = java.net.URL(['file:' jarsFullPath{i}]);
        stack.push(url);
        
    end
    
    urls = javaArray('java.net.URL',1);
    
    ucp2=sun.misc.URLClassPath(stack.toArray(urls));
    
    ucpf.set(cl, ucp2);
    
    for i=1:length(jars)
        javaaddpath(jarsFullPath{i});
    end
    
    java.lang.System.setProperty('java.rmi.server.RMIClassLoaderSpi','default');
    nl = org.jruby.common.NullWarnings();
    rl = com.sun.script.jruby.JRubyScriptEngine();
    
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

