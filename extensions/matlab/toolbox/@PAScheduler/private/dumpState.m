% dumpState dumps the current TaskRepository state to the disconnected mode
% file
function dumpState()
    opt = PAoptions();
    sched = PAScheduler;
    if isnumeric(opt.CustomDataspaceURL) && isempty(opt.CustomDataspaceURL)
        helper = org.ow2.proactive.scheduler.ext.matsci.client.DataspaceHelper.getInstance();
        registryurl = helper.getUrl();
    else
        registryurl = [];
    end
    sched.PATaskRepository('save');
    save(opt.DisconnectedModeFile, 'registryurl', '-append');
end