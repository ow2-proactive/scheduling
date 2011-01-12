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
function display(P)

s = size(P);
for i = 1:s(1)
    for j = 1:s(2)
        dp(P(i,j), inputname(1), i, j)
    end
end

end

function dp(X,name,k,l)
if isequal(get(0,'FormatSpacing'),'compact')
    if length(name) > 0
        disp([name '(' num2str(k) ',' num2str(l) ')' ' =']);
    end
    dp2(X.Func,'Func')
    dp2(X.Params,'Params')
    dp2(X.Description,'Description')
    dp2(X.InputFiles,'InputFiles')
    dp2(X.OutputFiles,'OuputFiles')
    dp2(X.Compose,'Compose')
    dp2(X.SelectionScript,'SelectionScript')
else
    disp(' ')
    if length(name) > 0
        disp([name '(' num2str(k) ',' num2str(l) ')' ' =']);
        disp(' ');
    end
    dp2(X.Func,'Func')
    dp2(X.Params,'Params')
    dp2(X.Description,'Description')
    dp2(X.InputFiles,'InputFiles')
    dp2(X.OutputFiles,'OuputFiles')
    dp2(X.Compose,'Compose')
    dp2(X.SelectionScript,'SelectionScript')
end
end

function dp2(Y,name)
if isnumeric(Y) && isempty(Y)
    return;
end
spacing=get(0,'FormatSpacing');
format compact
if iscell(Y)
    T = evalc('disp(Y);');
    disp(['    ' name ': ' strtrim(T)]);
elseif isa(Y,'function_handle')
    disp(['    ' name ':       @' char(Y)]);
elseif islogical(Y)
    if Y
        disp(['    ' name ':       true']);    
    else
        disp(['    ' name ':       false']);
    end
else
    try 
    disp(['    ' name ': ' char(Y)]);
catch
    disp(['    ' name ': ']);
    disp(Y);
end
    
end

set(0,'FormatSpacing',spacing);
end
