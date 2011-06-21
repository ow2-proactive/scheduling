%   PAresult() - constructor of PAresult object
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
function varargout = PAResult(varargin)
if nargin > 0    
    this.future = varargin{1};
    taskinfo = varargin{2};
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
    
    
else
    this.future = [];    
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






