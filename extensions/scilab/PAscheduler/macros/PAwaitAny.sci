function [val_k,index]=PAwaitAny(l,timeout)
    global('PA_solver')
    val_k = [];
    if typeof(l) == 'PAResL' then         
        jimport java.util.ArrayList;    
        jimport java.lang.Integer;   
        R=l.matrix(1).entries;
        jobid = R.jobid;   
        taskids = jnewInstance(ArrayList);
        indList=list();    
        m = size(l.matrix,2);
        for i=1:m
            pares=l.matrix(i).entries;
            if ~jexists(pares.waited) then
                error('PAResult::object cleared');
            end
            if ~jinvoke(pares.waited,'get')
                jinvoke(taskids,'add',pares.taskid);
                indList($+1)=i;           
            end
        end
        if isempty(indList)
            error('All results have already been accessed');
        end

        if argn(2) == 2
            // Because of this error in JIMS:
            // Method invocation: An error occurred during the data retrieving in Java:
            // Too many possible methods named waitForAny in the class org.objectweb.proactive.api.PAFuture.
            // timeout is for now ignored in PAwaitAny
            //ind = PAFuture.waitForAny(arrayList,timeout);
            tout = jinvoke(Integer,'parseInt',string(timeout));
            unrei = jinvoke(PA_solver,'waitAny',jobid, taskids,tout);
        else
            tout = jinvoke(Integer,'parseInt',string(-1));
            unrei = jinvoke(PA_solver,'waitAny',jobid, taskids,tout);
        end
        pair = jinvoke(unrei,'get');
        ind = jinvoke(pair,'getY');
        jremove(taskids);
        //jremove(ArrayList,PAFuture);
        j=indList(ind+1);
        index = j;
        pares=l.matrix(j).entries;
        jinvoke(pares.RaL,'set', jinvoke(pair,'getX'));
        jinvoke(pares.waited,'set',%t);
        [val_k,err] = PAResult_PAwaitFor(pares);
        if ~isempty(err) then
            warning(err);
            warning('PAwaitAny:Error occured')
        end
    else        
        error('Expected argument of type PAResL, received '+typeof(l))
    end
endfunction