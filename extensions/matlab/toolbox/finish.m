opt = PAoptions();
try
    sched = PAScheduler;
    tmpsolver = sched.PAgetsolver();
    if strcmp(class(tmpsolver), 'double')
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
            if isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
                helper = org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.getInstance();
                registryurl = helper.getUrl();
            else
                registryurl = [];
            end
            sched.PATaskRepository('save');
            save(opt.DisconnectedModeFile, 'registryurl', '-append');
            return;
        end
    end

    alljobs = sched.PATaskRepository('alljobs');
    for i = 1:length(alljobs)
        sched.PAaddFileToClean(alljobs{i});
    end
    for i = 1:length(alljobs)
        sched.PAaddDirToClean(alljobs{i});
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




