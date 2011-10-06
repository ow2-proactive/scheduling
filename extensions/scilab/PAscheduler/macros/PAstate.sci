function [] = PAstate()
    global ('PA_connected');
    if ~exists('PA_connected') | PA_connected ~= 1
        error('A connection to the ProActive scheduler must be established in order to use PAsolve, see PAconnect');
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    solver = jnewInstance(ScilabSolver);        
    env = jinvoke(solver,'getEnvironment');
    jinvoke(env, 'schedulerState');    
    jremove(solver,env,ScilabSolver);
endfunction