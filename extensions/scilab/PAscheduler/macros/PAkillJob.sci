function PAkillJob(jobid)
    global ('PA_connected');
    if ~exists('PA_connected') | PA_connected ~= 1
        error('A connection to the ProActive scheduler must be established in order to use PAsolve, see PAconnect');
    end
    if or(type(jobid)==[1 5 8]) then
        jobid = string(jobid);
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    solver = jnewInstance(ScilabSolver);            
    env = jinvoke(solver,'getEnvironment');
    jinvoke(env,'killJob',jobid);
    jremove(solver);
endfunction