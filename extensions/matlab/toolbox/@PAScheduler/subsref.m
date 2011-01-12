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

str=indexstr(S); %Converts S into an equivalent indexing expression
%contained in a string.

if nargout >= 1
    outstr = '[';
    for i = 1:nargout
        outstr = [outstr 'varargout{' num2str(i) '} '];
    end
    outstr = [outstr ']'];
    eval([outstr '=this' str ';']) ;
    %varargout=eval(['{this' str '};']) ;
else
    eval(['this' str ';']) ;
end


function st=indexstr(K)
name=inputname(1);

%DEFAULT
if isempty(name), name='S'; end
st='';
for ii=1:length(K)
    switch K(ii).type
        case '.'
            st=[st '.' K(ii).subs];
        case '()'
            insert=[name '(' num2str(ii) ')'];
            st=[st '(' insert '.subs{:})'];
        case '{}'
            insert=[name '(' num2str(ii) ')'];
            st=[st '{' insert '.subs{:}}'];
    end
end