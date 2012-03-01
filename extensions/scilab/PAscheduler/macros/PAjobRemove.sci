function PAjobRemove(jobid)
    global ('PA_connected','PA_solver');
    if ~exists('PA_connected') | PA_connected ~= 1
        error('A connection to the ProActive scheduler must be established in order to use PAsolve, see PAconnect');
    end
    if or(type(jobid)==[1 5 8]) then
        jobid = string(jobid);
    end        
    txt = jinvoke(PA_solver,'jobRemove',jobid);    
    printf('%s\n',txt);
    //jremove(ScilabSolver);
endfunction