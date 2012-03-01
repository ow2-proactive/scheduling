function [] = PAstate()
    global ('PA_connected','PA_solver');
    if ~PAisConnected()
        error('A connection to the ProActive scheduler is not established, see PAconnect');
    end
    
    txt = jinvoke(PA_solver, 'schedulerState');    
    printf('%s\n',txt);   
endfunction