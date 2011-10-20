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
function varargout = subsref(this,S)

val{1}=this;
for i=1:length(S)
    s=S(i);
    switch s.type
        case '.'
            sz=size(val{i});
            for l=1:sz(1)
                for m=1:sz(2)
                    Z=val{i}(l,m);
                    val{i+1}{(l-1)*sz(2)+m} = builtin('subsref', Z, s);                    
                end
            end
        case '()'
            %j=s.subs{1};
            K.type='()';
            K.subs=s.subs;
            val{i+1} = builtin('subsref', val{i}, K);
        otherwise
            error([s.type ,' not supported'])
    end
end
n = numel(val{i});
if n > 1
    %length(varargout) > 1 && nargout <= 1
    if iscell(val{i+1})
        varargout = cell(1,n);
        for j = 1 : n
            varargout{j} = val{i+1}{j};
        end
    else
        varargout = { val{i+1} };
    end
else
    if iscell(val{i+1})
        varargout = {val{i+1}{1}};
    else
        varargout = {val{i+1}};
    end
end
