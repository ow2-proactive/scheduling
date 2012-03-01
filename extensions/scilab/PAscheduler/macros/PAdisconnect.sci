function PAdisconnect()
    
    global ('PA_initialized', 'PA_connected', 'PA_solver')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end    
    if ~exists('PA_connected') | PA_connected ~= 1 | ~jinvoke(PA_solver,'isLoggedIn')         
        error('This Matlab session is not connected to a Scheduler');
    end
    try
       jinvoke(PA_solver ,'disconnect'); 
       PA_connected = %f;
    catch                 
    end     
    disp("Disconnected")       
endfunction