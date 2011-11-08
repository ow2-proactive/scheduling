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
opt = PAoptions();
try
    sched = PAScheduler;
    

    solver = sched.PAgetsolver();
    if strcmp(class(solver),'double')
        return;
    end
    if ~PAisConnected()
        warning('The matlab session is not connected to the scheduler, complete cleaning cannot be done');
        pause(1);
        return;
    end    
    jobs = sched.PATaskRepository('uncomplete');

    if length(jobs) > 0
        msg = ['The following tasks have not been retrieved : ' 10];
        for i = 1:length(jobs)
            msg = [msg 'Job ' jobs{i} ' : '];
            tsks = sched.PATaskRepository(jobs{i},'toreceive');
            for j = 1:length(tsks)
                msg = [msg tsks{j} ' '];
            end
            msg = [msg 10];
        end
        msg = [msg 'Do you want to enable disconnected mode ?'];
        button = questdlg(msg,'Disconnect','Yes','No','Yes');
        if strcmp(button, 'Yes')
            sched.dumpState();
            return;        
        end
    end

    alljobs = sched.PATaskRepository('alljobs');
    % it might be faster to clean dirs before files
    for i = 1:length(alljobs)
        sched.PAaddDirToClean(alljobs{i});
    end
    for i = 1:length(alljobs)
        sched.PAaddFileToClean(alljobs{i});
    end
    if isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
        helper = org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.getInstance();
        helper.shutdown();
    end

    if exist(opt.DisconnectedModeFile,'file')
        delete(opt.DisconnectedModeFile);
    end

catch ME
    disp('There was a problem during the finish script. Displaying the error during 10 seconds...');
    if isa(ME,'MException')
        disp(getReport(ME));
    elseif isa(ME, 'java.lang.Throwable')
        ME.printStackTrace();
    end
    if exist(opt.DisconnectedModeFile,'file')
        delete(opt.DisconnectedModeFile);
    end
    pause(10);
end




