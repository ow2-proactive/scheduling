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
function [ok, msg]=TestBigArrayAndKeepEngine(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 1600000;
    else
        timeout =800000;
    end
end
opt = PAoptions;
oldval = opt.KeepEngine;
PAoptions('KeepEngine',true);
disp('.................................. Testing PAsolve with big array and Keep Engine');
disp('............................First create a out of memory error on distant engines');
resl = PAsolve(@makeBigArray,{100000},{100000},{100000},{100000});
try 
val=PAwaitAll(resl,timeout)
catch err
end

disp('....Then create more big matrixes on the same engines (we verify that they recover)');
for i=1:8
    disp(['..........................Iteration ' num2str(i)]);
    resl = PAsolve(@makeBigArray,{i*1000},{i*1000},{i*1000},{i*1000});
    val=PAwaitAll(resl,timeout)
    for j=1:length(val)
        if val{j} ~= 1
            ok=false;
            msg='TestBigArrayAndKeepEngine::Some tasks didn''t succeed';
            return;
        end        
    end
    disp(['...........................' num2str(i) ' ......OK']);
    PAoptions('KeepEngine',oldval);
    ok=true;
    msg=[];
end
