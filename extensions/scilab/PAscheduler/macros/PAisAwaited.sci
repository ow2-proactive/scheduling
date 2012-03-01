function tf=PAisAwaited(l)
    global('PA_solver')
    if typeof(l) == 'PAResL' then
        m = size(l.matrix,2);
        R=l.matrix(1).entries;
        jobid = R.jobid;
        jimport java.util.ArrayList;
        taskids = jnewInstance(ArrayList);
        for i=1:m
            R = l.matrix(i).entries;            
            jinvoke(taskids,'add',R.taskid); 
        end
        unrei = jinvoke(PA_solver,'areAwaited',jobid, taskids);        
        answers = jinvoke(unrei,'get');
        for i=1:m
            pares=l.matrix(i).entries;
            tf(i)=jinvoke(answers,'get', i-1);
        end
    else
        error('Expected argument of type PAResL, received '+typeof(l))
    end
endfunction