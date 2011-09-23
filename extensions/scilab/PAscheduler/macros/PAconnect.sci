function [] = PAconnect(uri)

    global ('PA_initialized', 'PA_connected')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end
    jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
    solver = jnewInstance(ScilabSolver);
    if ~exists('PA_connected') | PA_connected ~= 1 | ~jinvoke(solver,'isLoggedIn')          
        jimport java.lang.String;
        jimport java.lang.System;
        jimport org.scilab.modules.gui.utils.ScilabPrintStream;
        if jinvoke(ScilabPrintStream,'isAvailable') then
           jinvoke(System, 'setOut',jinvoke(ScilabPrintStream,'getInstance'));
           jinvoke(ScilabPrintStream,'setRedirect',[]);
        end
        uristring = String.new(uri);        
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
