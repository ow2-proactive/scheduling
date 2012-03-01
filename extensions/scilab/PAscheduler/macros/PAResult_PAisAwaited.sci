function tf=PAResult_PAisAwaited(l)   
    global('PA_solver') 
    jobid = l.jobid;
    jimport java.util.ArrayList;
    taskids = jnewInstance(ArrayList);
                  
    jinvoke(taskids,'add',l.taskid); 
        
    unrei = jinvoke(PA_solver,'areAwaited',jobid, taskids);        
    answers = jinvoke(unrei,'get');
    
    tf=jinvoke(answers,'get', 0);
           
    //jremove(PAFuture);
endfunction