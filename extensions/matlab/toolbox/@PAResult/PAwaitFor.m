% PAResult/PAwaitFor blocks matlab execution until a set of results are available
%
% Syntax
%       >> val=PAwaitFor(r)
% 
% Inputs
%
%   r - an array of PAResult objects received by a call to PAsolve
%
% Outputs
%   
%   val - if r is a scalar, val contains the real result of the
%       computation. If r is a vector, then val will contain a cell array containing the real results.
%
% Description
%
%   PAResult/PAwaitFor will block matlab execution while waiting for a given set of
%   results. PAResult/PAwaitFor will wait until every results of the given
%   set have been computed.
%
% Example
%
%   >> r=PAsolve(@factorial, 1, 2, 3, 4);
%   >> val = PAwaitFor(r)  % Blocks Matlab execution until factorial(1), ..
%   , factorial(4) have been computed remotely and returns the results as
%   val = {factorial(1), .. , factorial(4)}
%
% See Also
%   PAsolve, PAResult/PAwaitAny, PAResult/PAisAwaited
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
function varargout = PAwaitFor(this,timeout)
s=size(this);
sched = PAScheduler;
% Get the solver from memory
solver = sched.PAgetsolver();
exception = [];
R=this(1,1);
jobid = R.jobid;
taskids = java.util.ArrayList(s(1)*s(2));
allRes = true;
for i=1:s(1)
    for j=1:s(2)
        R=this(i,j);   
        allRes = allRes && (R.resultSet.get() || R.waited.get());
        taskids.add(R.taskid); 
    end
end
if ~allRes
    if exist('timeout','var') == 1
        unrei = solver.waitAll(jobid,taskids,java.lang.Integer(timeout));    
    else
        unrei = solver.waitAll(jobid,taskids,java.lang.Integer(-1));
    end
    answers = unrei.get();
end
for i=1:s(1)
    for j=1:s(2)
        R=this(i,j);   
        if ~allRes
            RaL = answers.get((i-1)*s(2)+(j-1)); 
            R.RaL.set(RaL);
        else
            RaL = R.RaL.get();
        end    
        if RaL.isOK()
            printLogs(RaL,R,false);           

            if R.resultSet.get()
                A{i,j} = R.resultAcc();
            else
                load(R.outFile);
                A{i,j} = out;
                R.resultAcc(out);
                resultSet(R);
            end             

        elseif RaL.isMatSciError();
            printLogs(RaL,R,true);  
            if ~R.resultSet.get()  
                R.iserror.set(true);      
                resultSet(R);
            end
            exception = MException('PAResult:PAwaitFor','Error during remote script execution');
        else
            printLogs(RaL,R,true); 
            e = RaL.getException();
            err = java.lang.System.err;
            e.printStackTrace(err);
            if ~R.resultSet.get()                
                R.iserror.set(true);
                resultSet(R);
            end
            exception = MException('PAResult:PAwaitFor','Internal Error');
        end                       
    end
end

if isa(exception,'MException')
    throw(exception);
end
if isscalar(A)
    A=A{1};
end
if nargout <= 1
    varargout{1}=A;
else
    for i=1:s(1)
        for j=1:s(2)
            varargout{(i-1)*s(2)+j} = A{i,j};
        end
    end
end
end

function resultSet(R)
R.resultSet.set(true);
sched = PAScheduler;
sched.PATaskRepository(R.jobid, R.taskid, 'received');
clean(R);
end

function printLogs(RaL,R,err)
if ~R.logsPrinted.get() || err
    logs = RaL.getLogs();
    if isjava(logs)
        R.logs.append(logs);
        R.logsPrinted.set(true);
        if logs.length() > 0
            if (err)
                java.lang.System.err.println(logs);
            else
                java.lang.System.out.println(logs);
            end
        end
    end
end
end








