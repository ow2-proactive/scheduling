function [] = PAstate()
    global ('PA_connected');
    if ~PAisConnected()
        error('A connection to the ProActive scheduler is not established, see PAconnect');
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    solver = jnewInstance(ScilabSolver);        
    env = jinvoke(solver,'getEnvironment');
    jinvoke(env, 'schedulerState');    
    jremove(solver,env,ScilabSolver);
endfunction