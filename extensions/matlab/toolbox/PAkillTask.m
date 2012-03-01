% PAkillJob kills the given Scheduler task
%
% Syntax
%
%       >> PAkillTask(jobid, tasmname);
%
% Inputs
%       
%       jobid - the id of the job (string or numeric)
%       taskname - the name of the task (string)
%
%
% Description
%
%       PAkillTask kills the given Scheduler task of the given Scheduler job.
%
% See also
%       PAkillJob, PApauseJob, PAresumeJob


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
function PAkillTask(jobid, tname)
    if isnumeric(jobid)
        jobid = num2str(jobid);
    end
    if ~PAisConnected()
        error('A connection to the ProActive scheduler is not established, see PAconnect');
    end
    sched = PAScheduler;
    solver = sched.PAgetsolver();
    output = solver.killTask(jobid, tname);
    disp(output);
end