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

function res = PAsolve_modelname(params)
bdclose all
warning off all
rightpwd = params.pwd_dir; 
cd(rightpwd)
%pwd
set_param(0,'CharacterEncoding', 'ISO-8859-1');
assignin('base','params',params);
assignin('base','str_cur_param',params.pwd_dir);
evalin('base',[params.pwd_dir '()']);
options = simset('SrcWorkspace', 'base', 'DstWorkspace','base');
modelName = params.modelName;
modelName = modelName(1:end-4);
load_system(modelName);
set_param(modelName,'StopTime',num2str(params.end_time));
%saving a file containing the infomation about the tolerance
mat_blocks = find_system(modelName,'BlockType','ToFile');
%if length(mat_blocks) > 0
%    for i = 1:length(mat_blocks)
%        tol(i) = get_param(mat_blocks{i},'Sample time')
%    end
%    tolerance = min(tol);
%    save([params.current_dir filesep 'tol.mat'], 'tolerance');
%end
    


save_system(modelName);
close_system(modelName);

sim(params.modelName,[],options);
    
bdclose all
res = 1;
