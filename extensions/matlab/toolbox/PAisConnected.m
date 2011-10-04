function tf=PAisConnected()
% PAisConnected tells if this Matlab session is connected to a ProActive
% Scheduler
%
% Syntax
%
%       tf=PAisConnected();
%
%
% Ouputs
%
%       tf - boolean value, true if this Matlab session is connected to a
%       ProActive Scheduler
%
% Description
%
%       PAisConnected tells if this Matlab session is properly connected to a
%       ProActive Scheduler.
%
%
% See also
%   PAconnect, PAdisconnect
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
if strcmp(class(tmpsolver), 'double')
    tf = false;
    return;
end

try 
   tmpsolver.isConnected();
catch ME
   tf = false;
   return;
end

if tmpsolver.isLoggedIn()
    tf = true;
    return;
end
tf = false;