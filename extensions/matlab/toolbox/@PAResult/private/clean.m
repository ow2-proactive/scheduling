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
function clean(R)
if ~R.cleaned.get()
    opt = PAoptions();
    if opt.CleanAllTempFilesDirectly
        sched = PAScheduler;
        warning('off');
        setd = R.cleanDirSet;
        tsks = sched.PATaskRepository(R.jobid, 'toreceive');
        if length(tsks) == 0
            for i=1:length(setd)
                if exist(setd{i},'dir')
                    try
                        rmdir(setd{i},'s');
                    catch
                    end
                end
            end
        end        
        R.cleaned.set(1);
        warning('on');
    else
        sched = PAScheduler;
        warning('off');
        setf = R.cleanFileSet;
        for i=1:length(setf)
            if exist(setf{i},'file')
                delete(setf{i});
            end
        end
        warning('on');
        R.cleaned.set(1);
        
    end
end
