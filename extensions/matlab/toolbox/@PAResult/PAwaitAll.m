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
function varargout = PAwaitAll(this,timeout)
s=size(this);
arrayList = java.util.ArrayList();
for i=1:s(1)
    for j=1:s(2)        
      R=this(i,j);      
      arrayList.add(R.future);      
    end
end
if exist('timeout','var') == 1
    org.objectweb.proactive.api.PAFuture.waitForAll(arrayList,int64(timeout));
else
    org.objectweb.proactive.api.PAFuture.waitForAll(arrayList);
end
for i=1:s(1)
    for j=1:s(2) 
        R=this(i,j);
        if nargout > 1 || (s(1) == 1 && s(2) == 1)
            try
                varargout{(i-1)*s(2)+j} = PAwaitFor(R);            
            catch ME
                ME.stack;
                varargout{(i-1)*s(2)+j} = [];
            end
        else
            try
                varargout{1}{(i-1)*s(2)+j} = PAwaitFor(R); 
             catch ME
                ME.stack;
                varargout{1}{(i-1)*s(2)+j} = [];
            end   
        end
    end
end
