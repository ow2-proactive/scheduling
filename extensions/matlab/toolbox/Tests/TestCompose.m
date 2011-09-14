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
function [ok, msg]=TestCompose(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 500000;
    else
        timeout = 200000;
    end
end
format long
t = PATask(3,5);
t(1,1:5).Func = @mysqrt;
t(1,1).Params = 1;
t(1,2).Params = 2;
t(1,3).Params = 3;
t(1,4).Params = 4;
t(1,5).Params = 5;
t(2,1:5) = t(1,1:5);
t(2,1:5).Params = {};
t(2,1:5).Compose = true;
t(3,1:5) = t(1,1:5);
t(3,1:5).Params = {};
t(3,1:5).Compose = true;
t

 disp('...... Testing PAsolve with sqrt(sqrt(sqrt(x)))');
 disp('..........................1 PAwaitAll');
 resl = PAsolve(t);
 val=PAwaitAll(resl,timeout)
 [ok,msg]=checkValues(val);
if ~ok disp(msg),return; end
 disp('..........................1 ......OK');
 clear val;
 
 disp('..........................2 PAwaitAny');
resl = PAsolve(t);
for i=1:5
    val(i)=PAwaitAny(resl,timeout)
end
val=sort(val)
[ok,msg]=checkValues(val);
if ~ok disp(msg),return; end
disp('..........................2 ......OK');
clear val;

function [ok,msg]=checkValues(val)
right=num2cell(sqrt(sqrt(sqrt(1:5))))
if length(right) ~= length(val)
    ok = false;
    msg = 'Wrong number of outputs';
else
    for i=1:length(right)
        if iscell(val)
            if val{i} ~= right{i}
                ok = false;
                msg = ['TestCompose::Wrong value of sqrt(sqrt(sqrt(' num2str(i) '))), received ' num2str(val{i}) ', expected ' num2str(right{i})];
            else
                ok = true;
                msg = [];
            end
        else
            if val(i) ~= right{i}
                ok = false;
                msg = ['TestCompose::Wrong value of sqrt(sqrt(sqrt(' num2str(i) '))), received ' num2str(val(i)) ', expected ' num2str(right{i})];
            else 
                ok = true;
                msg = [];
            end
        end
    end

end



