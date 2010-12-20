%   PAgetResults() - results from a job
%
%   Usage:
%       >> res = PAgetResults(jobid);
%       >> res = PAgetResults('1')
%
%
%   Inputs:
%       
%       jobid - id of the job
%
%   Ouputs: 
%
%       res - an array of PAresult objects
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
function res = PAgetResults(jobid)
if ~ischar(jobid) && ~isnumeric(jobid)
    error('PAgetResults::JobId must be a string or a number');
end
if isnumeric(jobid)
    jobid = num2str(jobid);
end
sched = PAScheduler;
solver = sched.PAgetsolver();
if strcmp(class(solver),'double')
    error('connexion to the scheduler is not established');
end
jinfo = sched.PATaskRepository(jobid, 'jobinfo');
if isnumeric(jinfo) && isempty(jinfo)
    error(['PAgetResults::Unknown job : ' jobid]);
end
disp(['Retrieving results of job ' jobid]);
alltasks = sched.PATaskRepository(jobid, 'alltasks');
solver = sched.PAgetsolver();
resfutureList = solver.retrieve(jinfo);
for j=1:length(alltasks)
    taskinfo = sched.PATaskRepository(jobid, alltasks{j}, 'taskinfo');
    res(j)=PAResult(resfutureList.get(j-1), taskinfo);
    sched.PAaddDirToClean(jobid, taskinfo.cleanDirSet);
end