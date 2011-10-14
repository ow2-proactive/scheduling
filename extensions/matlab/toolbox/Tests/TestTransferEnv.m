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
function [ok, msg]=TestTransferEnv(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 200000;
    else
        timeout = 80000;
    end
end
disp('...... Testing PAsolve with Transfer Env');
disp('..........................1 PAwaitAll');
opt=PAoptions();
old = opt.TransferEnv;
PAoptions('TransferEnv', true);
mytransferenvvar = 'toto';
resl = PAsolve(@transferenvfunc,'titi');
val=PAwaitAll(resl,timeout)
if val
    disp('..........................1 ......OK');
    ok=true;
    msg = [];
else
    disp('..........................1 ......KO');
    ok=false;
    msg = 'TestTransferEnv::wrong value for val, error occured remotely';
end
PAoptions('TransferEnv', old);