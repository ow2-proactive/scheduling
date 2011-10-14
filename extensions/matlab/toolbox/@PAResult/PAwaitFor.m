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
exception = [];
for i=1:s(1)
    for j=1:s(2)
        R=this(i,j);
        f = R.future;
        if exist('timeout','var') == 1

            RaL = org.objectweb.proactive.api.PAFuture.getFutureValue(f,timeout);
        else
            RaL = org.objectweb.proactive.api.PAFuture.getFutureValue(f);
        end
        if RaL.isOK()
            printLogs(RaL,R,false);           

            if R.resultSet.get()
                A{i,j} = R.resultAcc();
            else
                load(R.outFile);
                A{i,j} = out;
                R.resultAcc(out);
                R.resultSet.set(true);
            end

        elseif RaL.isMatSciError();
            printLogs(RaL,R,true);  
            R.iserror.set(true);
            exception = MException('PAResult:PAwaitFor','Error during remote script execution');
        else
            printLogs(RaL,R,true); 
            e = RaL.getException();
            err = java.lang.System.err;
            e.printStackTrace(err);
            R.iserror.set(true);
            exception = MException('PAResult:PAwaitFor','Internal Error');
        end        
        if ~R.cleaned.get()
            clean(R);
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

function printLogs(RaL,R,err)
if ~R.logsPrinted.get()
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








