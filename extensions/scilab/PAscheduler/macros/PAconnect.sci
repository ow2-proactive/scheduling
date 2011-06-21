function [] = PAconnect(uri)

    global ('PA_initialized', 'PA_connected')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end
    if ~exists('PA_connected') | PA_connected ~= 1  
        jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
        jimport java.lang.String;
        uristring = String.new(uri);
        solver = jnewInstance(ScilabSolver);
        ex = solver.createConnection(uristring);
        if type(ex) == 10 then            
            disp(ex); 
        else
            PA_connected = 1;
            disp(strcat(['Connection successful to ', uri]));
        end        
        jremove(uristring,solver);
    else
        disp('Already connected');
    end

endfunction
