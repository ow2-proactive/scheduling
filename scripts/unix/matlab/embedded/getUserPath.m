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
function upath = getUserPath()
root = matlabroot;
allpaths = path;
p = 1;
upath = '';
while true
    t = strtok(allpaths(p:end), pathsep);
    if isempty(strfind(t, root));
        if (isempty(upath))
            upath=t;
        else
            upath=strcat(upath,pathsep,t);
        end
    end
    p = p + length(t) + 1;
    if isempty(strfind(allpaths(p:end),pathsep)) break, end;
end