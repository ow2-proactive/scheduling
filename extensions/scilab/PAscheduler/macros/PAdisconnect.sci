function PAdisconnect()
    
    global ('PA_initialized', 'PA_connected')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    solver = jnewInstance(ScilabSolver);
    if ~exists('PA_connected') | PA_connected ~= 1 | ~jinvoke(solver,'isLoggedIn') 
        jremove(solver,ScilabSolver);
        error('This Matlab session is not connected to a Scheduler');
    end
    try
       jinvoke(solver ,'disconnect'); 
       PA_connected = %f;
    catch                 
    end        
    jremove(solver,ScilabSolver);
endfunction