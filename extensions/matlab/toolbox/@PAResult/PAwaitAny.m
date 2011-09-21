% PAResult/PAwaitAny blocks matlab execution until any result is available in a
% given array of PAResult object
%
% Syntax
%       >> val=PAwaitAny(r)
% 
% Inputs
%
%   r - an array of PAResult objects received by a call to PAsolve
%
% Outputs
%   
%   val - contains the real result of the computation.
%
% Description
%
%   PAResult/PAwaitAny will block matlab execution while waiting for a given set of
%   results. PAResult/PAwaitAny differs from PAResult/PAwaitFor in that it will wait for the
%   first result available and returns this result, allowing
%   post-processing treatments to be executed immediately without having to
%   wait for a large set of results. Further calls to PAwaitAny will return
%   the remaining results until all results have been computed.
%
% Example
%
%   >> r=PAsolve(@factorial, 1, 2, 3, 4);
%   >> val = PAwaitAny(r)  % Blocks Matlab execution until either factorial(1), ..
%   , or factorial(4) has been computed remotely and returns the result as
%   val = factorial(i)
%   >> val2 = PAwaitAny(r) % Blocks for the second result, etc ...
%
% See Also
%   PAsolve, PAResult/PAwaitFor, PAResult/PAisAwaited
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
function A = PAwaitAny(this,timeout)
s=size(this);
arrayList = java.util.ArrayList();
indList={};
k=1;
for i=1:s(1)
    for j=1:s(2)
        R=this(i,j);
        if ~R.waited.get()
            arrayList.add(R.future);
            indList{k}=[i j];
            k=k+1;
        end
    end
end

if isempty(indList)
    error('All results have already been accessed');
end

if exist('timeout','var') == 1
    ind = org.objectweb.proactive.api.PAFuture.waitForAny(arrayList,int64(timeout));
else
    ind = org.objectweb.proactive.api.PAFuture.waitForAny(arrayList);
end
l=indList{ind+1};
R=this(l(1),l(2));
R.waited.set(true);
A = PAwaitFor(R);


