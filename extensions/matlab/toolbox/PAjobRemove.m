% PAjobRemove removes the given job from the scheduler
%
% Syntax
%
%       >> PAjobRemove(jobid);       
%
% Inputs
%       
%       jobid - the id of the job (string or numeric)
%
% Description
%
%       PAjobRemove removes the given job from the scheduler. By default,
%       the job is automatically removed from the scheduler when all
%       results have been read. The option RemoveJobAfterRetrieve can be
%       set to false to manually remove the job with the PAjobRemove
%       function.
%
% See also
%       PAjobResult, PAtaskResult, PAtaskOutput

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
function PAjobRemove(jobid)
    if isnumeric(jobid)
        jobid = num2str(jobid);
    end
    if ~PAisConnected()
        error('A connection to the ProActive scheduler is not established, see PAconnect');
    end
    sched = PAScheduler;
    solver = sched.PAgetsolver();
                
    solver.jobRemove(jobid);
    
end