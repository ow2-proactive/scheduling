function [] = PAconnect(uri,credpath)

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
        if argn(2) == 2
            ex = jinvoke(solver, 'createConnection',uri, credpath);
        else       
            ex = jinvoke(solver, 'createConnection',uri, []);
        end        
        if type(ex) == 10 then            
            disp(ex); 
            jremove(solver);
            error('PAconnect::Error while connecting');
        else
            PA_connected = 1;
            disp(strcat(['Connection successful to ', uri]));
        end                
    else
        disp('Already connected');
    end
    jremove(solver);
endfunction
