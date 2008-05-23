% getUserPath() - Extracts from the current Matlab session the user-only path (excluding any Matlab toolbox system path)
%
% Usage:
%   >> upath = getUserPath();
%
% Outputs:
%   upath - user string path
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
function varagout = grabProActiveLibrary()
[pathstr, name, ext, versn] = fileparts(mfilename('fullpath'));

jarUrl = java.net.URL('http://proactive.inria.fr/userfiles/file/ProActiveRCP/test/ProActiveRCP.jar');
connection = jarUrl.openConnection();
connection.setRequestMethod('HEAD');
lmremote = int64(connection.getHeaderFieldDate('Last-Modified',0));


localFile = java.io.File(strcat(pathstr,filesep,'lib',filesep,'ProActive.jar'));

if localFile.exists()
    lmlocal = int64(localFile.lastModified());
    
    if lmremote > lmlocal
        unzipRemoteJar(pathstr, lmremote);
    end
else
    unzipRemoteJar(pathstr, lmremote);
end
end

function varargout = unzipRemoteJar(pathstr, lmremote)
    disp('Retrieving ProActive library this might take a while...');
        unzip('http://proactive.inria.fr/userfiles/file/ProActiveRCP/test/ProActiveRCP.jar',strcat(pathstr,filesep,'lib')); 
        localFile = java.io.File(strcat(pathstr,filesep,'lib',filesep,'ProActive.jar'));
        localFile.setLastModified(lmremote);
end






