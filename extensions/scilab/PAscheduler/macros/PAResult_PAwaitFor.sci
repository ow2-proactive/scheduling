function val=PAResult_PAwaitFor(R,timeout)
    global ('PAResult_DB');
    //disp('PAResult_PAwaitFor : init')    
    errormessage=[];
    if ~jexists(R.future) then
        error('PAResult::object cleared');        
    end
    jimport org.objectweb.proactive.api.PAFuture;
    ff = R.future;    
    if argn(2) == 2
        RaL = PAFuture.getFutureValue(ff,timeout);
    else
        RaL = PAFuture.getFutureValue(ff);
    end
    if jinvoke(RaL,'isOK')
        //disp('PAResult_PAwaitFor : isOK')        
        printLogs(R,RaL);

        if jinvoke(R.resultSet,'get')
            //disp('PAResult_PAwaitFor : Result get')
            val=PAResult_DB(R.dbrid);
            //disp(val)
        else
            //disp('PAResult_PAwaitFor : Result set')
            load(R.outFile);
            val = out;
            PAResult_DB(R.dbrid) = out;
            //disp(val)
            resultSet(R);
        end

    elseif jinvoke(RaL,'isMatSciError');
        //disp('PAResult_PAwaitFor : isMatSciError')
        
        printLogs(R,RaL);
        jinvoke(R.iserror,'set',%t);
        resultSet(R);
        errormessage = 'PAResult:PAwaitFor Error during remote script execution';
    else
        //disp('PAResult_PAwaitFor : internalError')
        printLogs(R,RaL);
        e = jinvoke(RaL,'getException');
        jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
        exstr = ScilabSolver.getStackTrace(e);
        printf('%s',exstr);
        try
            jremove(e);
            jremove(exstr);
            //jremove(ScilabSolver);
        catch 
        end        
        jinvoke(R.iserror,'set',%t);
        resultSet(R);
        errormessage = 'PAResult:PAwaitFor Internal Error';
    end
    jremove(RaL);
    //jremove(PAFuture);

    PAResult_clean(R);
    if errormessage ~= [] then
        val = [];
        error(errormessage)
    end
endfunction

function resultSet(R)
    global('PAResult_TasksDB')
    jinvoke(R.resultSet,'set',%t);
    remainingTasks = PAResult_TasksDB(R.sid);
    ind = -1;
    for i=1:length(remainingTasks)
        if remainingTasks(i) == R.taskid then
            ind = i;            
        end
    end    
    if ind > 0 then
        remainingTasks(ind) = null();
    end
    
    PAResult_TasksDB(R.sid) = remainingTasks;
    opt = PAoptions();
    if opt.RemoveJobAfterRetrieve & length(remainingTasks) == 0 then
        PAjobRemove(R.jobid);
    end
endfunction

function printLogs(R,RaL)
    if ~jexists(R.logsPrinted) then
            error('PAResult::object cleared');
        end
    if ~jinvoke(R.logsPrinted,'get')
        logs = jinvoke(RaL,'getLogs');
        dummy = jinvoke(R.logs,'append',logs);
        jremove(dummy); // append returns a StringBuilder object that must be freed
        logstr = jinvoke(R.logs,'toString');
        if ~isempty(logstr) then
            printf('%s',logstr);
        end
        jinvoke(R.logsPrinted,'set',%t);
        jremove(logs,logstr);

    end    
endfunction