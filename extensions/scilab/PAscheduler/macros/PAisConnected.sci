function tf=PAisConnected()
    
    global ('PA_initialized', 'PA_connected','PA_solver')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end       
    if ~exists('PA_connected') | PA_connected ~= 1
        tf = %f;        
        return;
    end   
    try
       tf = jinvoke(PA_solver ,'isConnected'); 
    catch 
        tf = %f;                
        return;
    end    
    if ~tf then        
        return;
    end
    tf = jinvoke(PA_solver ,'isLoggedIn');     
    return;
endfunction

