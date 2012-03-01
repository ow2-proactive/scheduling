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
function [ok, msg]=TestMultipleSubmit(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 500000;
    else
        timeout = 200000;
    end
end

disp('...... Testing PAsolve with multiple submits');

disp('..........................submit 1');
resl1 = PAsolve(@factorial,{1},{2},{3},{4},{5});
disp('..........................submit 2');
resl2 = PAsolve(@factorial,{1},{2},{3},{4},{5});   
disp('..........................submit 3');
resl3 = PAsolve(@factorial,{1},{2},{3},{4},{5}); 
disp('..........................submit 4');
resl4 = PAsolve(@factorial,{1},{2},{3},{4},{5});  
disp('..........................submit 5');
resl5 = PAsolve(@factorial,{1},{2},{3},{4},{5});  

val1=PAwaitFor(resl1,timeout)
val2=PAwaitFor(resl2,timeout)
val3=PAwaitFor(resl3,timeout)
val4=PAwaitFor(resl4,timeout)
val5=PAwaitFor(resl5,timeout)

[ok,msg]=checkValuesFact(val1);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val2);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val3);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val4);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val5);
if ~ok disp(msg),return; end

function [ok,msg]=checkValuesFact(val)
[ok,msg]=checkValues(val,{1,2,6,24,120},'factorial');

function [ok,msg]=checkValues(val,right,name)
if length(right) ~= length(val)
    ok = false;
    msg = 'Wrong number of outputs';
else
    for i=1:length(right)
        if iscell(val)
            if val{i} ~= right{i}
                ok = false;
                msg = ['TestMultipleSubmit::Wrong value of ' name '(' num2str(i) '), received ' num2str(val{i}) ', expected ' num2str(right{i})];
            else
                ok = true;
                msg = [];
            end
        else
            if val(i) ~= right{i}
                ok = false;
                msg = ['TestMultipleSubmit::Wrong value of ' name '(' num2str(i) '), received ' num2str(val(i)) ', expected ' num2str(right{i})];
            else 
                ok = true;
                msg = [];
            end
        end
    end

end