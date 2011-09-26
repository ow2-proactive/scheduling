function tf=PAisConnected()
    
    global ('PA_initialized', 'PA_connected')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    solver = jnewInstance(ScilabSolver);
    if ~exists('PA_connected') | PA_connected ~= 1
        tf = %f;
        return;
    end
    try
       tf = jinvoke(solver ,'isConnected'); 
    catch 
        tf = %f;
        return;
    end    
    if ~tf then
        return;
    end
    tf = jinvoke(solver ,'isLoggedIn'); 
    return;
endfunction

