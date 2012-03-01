% PAResult constructor of PAResult objects
%
% Properties
%
%       jobid - id of the Scheduler job submitted when calling PAsolve and
%       receiving this PAResult object.
%
%       val - contains the result of the computation, if the result is not
%       yet available, calling the val property will result in blocking
%       Matlab execution until the result is computed.
%
%       logs - the textual log associated with this PAResult object (similarly 
%       to the val property, it can block matlab execution).
%       
%       isError - a boolean value indicating wether this result has
%       triggered an error in the remote execution or not.
%
%
% Methods
%
%       PAwaitFor - waits for the computation of the given PAResult array
%
%       PAwaitAny - waits until any result has been computed in the given
%       array of PAResult objects.
%
%       PAisAwaited - tells which results among the given array of PAResult
%       objects is available.
%
% See also
%       PAsolve, PAResult/PAwaitFor, PAResult/PAwaitAny, PAResult/PAisAwaited


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
function varargout = PAResult(varargin)
if nargin > 0       
    taskinfo = varargin{1};
    this.cleanFileSet = taskinfo.cleanFileSet;
    this.cleanDirSet = taskinfo.cleanDirSet;
    this.outFile = taskinfo.outFile;
    this.jobid = taskinfo.jobid;
    this.taskid = taskinfo.taskid;
    
        this.cleaned = java.util.concurrent.atomic.AtomicBoolean(false);
        this.logsPrinted = java.util.concurrent.atomic.AtomicBoolean(false);
        this.logs = java.lang.StringBuilder();
        this.waited = java.util.concurrent.atomic.AtomicBoolean(false);
        this.iserror = java.util.concurrent.atomic.AtomicBoolean(false);                       
        this.resultSet = java.util.concurrent.atomic.AtomicBoolean(false);    
        this.RaL =  org.ow2.proactive.scheduler.ext.matsci.client.common.data.UnReifiable();
    
    
else      
    this.cleanFileSet = [];
    this.cleanDirSet = [];
    this.outFile = [];
    this.jobid = 0;
    this.taskid = [];
    this.cleaned = java.util.concurrent.atomic.AtomicBoolean(true);
    this.logsPrinted = java.util.concurrent.atomic.AtomicBoolean(true);
    this.logs = java.lang.StringBuilder();
    this.waited = java.util.concurrent.atomic.AtomicBoolean(true);
    this.iserror = java.util.concurrent.atomic.AtomicBoolean(false);            
    this.resultSet = java.util.concurrent.atomic.AtomicBoolean(true);
    this.RaL =  org.ow2.proactive.scheduler.ext.matsci.client.common.data.UnReifiable();
end

result = [];
this.resultAcc = @accessResult; 

function out=accessResult(in)
if nargin, result = in; end
out = result;
end


for i=1:nargout
    varargout{i}=[];
    varargout{i} = class(this,'PAResult');
end
end






