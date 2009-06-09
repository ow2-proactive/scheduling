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
function varargout = PAprepare(varargin)

% Setting the English locale, mandatory for ptolemy
java.util.Locale.setDefault(java.util.Locale.ENGLISH);

if nargin == 1
    % If a path to proactive is specified, we use this path to locate
    % ProActive library
    [pathstr, name, ext, versn] = fileparts(varargin{1});
    if strcmp(ext,'jar')
        javaaddpath(varargin{1});

    else
        javaaddpath(strcat(pathstr,fileset,'ProActive.jar'));
    end
    if fopen(strcat(pathstr,filesep,'proactive-log4j'))
        log4jFile = java.io.File(strcat(pathstr,filesep,'lib',filesep,'proactive-log4j'));
        urlLog4jFile = log4jFile.toURI().toURL();
        java.lang.System.setProperty('log4j.configuration',urlLog4jFile.toExternalForm());
    end
else
    % Otherwise, we retrieve the last updated ProActive library from the
    % web
    %grabProActiveLibrary();

    [pathstr, name, ext, versn] = fileparts(mfilename('fullpath'));

    % Scheduler root
    javafile = java.io.File(pathstr);
    scheduling_dir = char(javafile.getParentFile().getParentFile().getParent().toString());



    % Log4J file
    log4jFile = java.io.File([scheduling_dir filesep 'config' filesep 'log4j' filesep 'log4j-client']);
    urlLog4jFile = log4jFile.toURI().toURL();
    java.lang.System.setProperty('log4j.configuration',urlLog4jFile.toExternalForm());

    % Policy
    java.lang.System.setProperty('java.security.policy',[scheduling_dir filesep 'config' filesep 'scheduler.java.policy']);

    % Dist libs
    dist_lib_dir = [scheduling_dir filesep 'dist' filesep 'lib'];

    proactiveset = 0;
    try
        ao = org.objectweb.proactive.api.PAActiveObject.newActive('org.objectweb.proactive.core.util.wrapper.StringWrapper',[]);
        proactiveset = 1;
    catch ME
    end

    if ~proactiveset

        if (ispc)
            old_dir = pwd;
            cd(dist_lib_dir);
            [s,dist_lib_dir] = dos('command.com /c cd');
            dist_lib_dir = strtrim(dist_lib_dir);
            [s,w] = dos('dir /x ProActive.jar');
            p1 = [dist_lib_dir filesep shortname(w,'ProActive.jar')];
            [s,w] = dos('dir /x ProActive_Scheduler-core.jar');
            p2 = [dist_lib_dir filesep shortname(w,'ProActive_Scheduler-core.jar')];
            [s,w] = dos('dir /x ProActive_ResourceManager.jar');
            p3 = [dist_lib_dir filesep shortname(w,'ProActive_ResourceManager.jar')];
            [s,w] = dos('dir /x ProActive_SRM-common.jar');
            p4 = [dist_lib_dir filesep shortname(w,'ProActive_SRM-common.jar')];
            cd(old_dir);
        else
            p1 = [dist_lib_dir filesep 'ProActive.jar'];
            p2 = [dist_lib_dir filesep 'ProActive_Scheduler-core.jar'];
            p3 = [dist_lib_dir filesep 'ProActive_ResourceManager.jar'];
            p4 = [dist_lib_dir filesep 'ProActive_SRM-common.jar'];

        end

        javaaddpath(p1);
        javaaddpath(p2);
        javaaddpath(p3);
        javaaddpath(p4);


    end
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

