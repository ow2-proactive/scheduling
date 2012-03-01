% dumpState dumps the current TaskRepository state to the disconnected mode
% file
function dumpState()
    opt = PAoptions();
    sched = PAScheduler;    
    sched.PATaskRepository('save'); 
    rmiport = opt.RmiPort;
    save(opt.DisconnectedModeFile, 'rmiport', '-append');
    
end