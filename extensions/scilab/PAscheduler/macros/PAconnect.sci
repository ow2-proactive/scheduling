function [] = PAconnect(uri)

    global ('PA_initialized', 'PA_connected')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end
    if ~exists('PA_connected') | PA_connected ~= 1  
        clzsolver = class('org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver');
        clzstring = class('java.lang.String');             
        uristring = newInstance(clzstring,uri);
        solver = newInstance(clzsolver);
        ex = invoke_u(solver,'createConnection',uristring);        
        if type(ex) == 10 then            
            disp(ex); 
        else
            PA_connected = 1;
            disp(strcat(['Connection successful to ', uri]));
        end        
        
    else
        disp('Already connected');
    end

endfunction