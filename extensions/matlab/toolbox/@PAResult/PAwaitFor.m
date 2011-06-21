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
            if ~R.logsPrinted.get()
                logs = RaL.getLogs();
                R.logs.append(logs);
                R.logsPrinted.set(true);
                java.lang.System.out.println(logs);
            end            

               if R.resultSet.get()
                  A{i,j} = R.resultAcc();
               else
                   load(R.outFile);
                   A{i,j} = out;
                   R.resultAcc(out);
                   R.resultSet.set(true);
               end

        elseif RaL.isMatSciError();
            if ~R.logsPrinted.get()
                logs = RaL.getLogs();
                R.logs.append(logs);
                R.logsPrinted.set(true);
                java.lang.System.err.println(logs);                
            end
            R.iserror.set(true);
            exception = MException('PAResult:PAwaitFor','Error during remote script execution');
        else
            if ~R.logsPrinted.get()
                logs = RaL.getLogs();
                if isjava(logs)
                    R.logs.append(logs);
                    R.logsPrinted.set(true);
                    java.lang.System.err.println(logs);
                end
            end
            e = RaL.getException();
            err = java.lang.System.err;
            e.printStackTrace(err);
            R.iserror.set(true);
            exception = MException('PAResult:PAwaitFor','Internal Error');            
        end
        clean(R);                  
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







