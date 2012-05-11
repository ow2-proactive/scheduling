function [val_k,err]=PAResult_PAwaitFor(R,RaL)
    global ('PAResult_DB');
    val_k = [];
    //disp('PAResult_PAwaitFor : init')    
    errormessage=[];
    //if ~jexists(R.future) then
    //    error('PAResult::object cleared');        
    //end
       
    if argn(2) == 2
        jinvoke(R.RaL,'set',RaL);        
    else
        RaL = jinvoke(R.RaL,'get');                
    end
    if jinvoke(RaL,'isOK')
        //disp('PAResult_PAwaitFor : isOK')        
        printLogs(R,RaL, %f);

        if jinvoke(R.resultSet,'get')
            //disp('PAResult_PAwaitFor : Result get')
            val_k=PAResult_DB(R.dbrid);
            //disp(val)
        else
            //disp('PAResult_PAwaitFor : Result set')
            load(R.outFile);
            val_k = out;
            PAResult_DB(R.dbrid) = out;
            //disp(val)
            resultSet(R);
        end

    elseif jinvoke(RaL,'isMatSciError');
        //disp('PAResult_PAwaitFor : isMatSciError')
        
        printLogs(R,RaL,%t);
        jinvoke(R.iserror,'set',%t);
        resultSet(R);
        errormessage = 'PAResult:PAwaitFor Error during execution of task '+R.taskid;
    else
        //disp('PAResult_PAwaitFor : internalError')
        printLogs(R,RaL,%t);
        e = jinvoke(RaL,'getException');
        jimport org.ow2.proactive.scheduler.ext.common.util.StackTraceUtil;        
        exstr = jinvoke(StackTraceUtil,'getStackTrace',e);
        printf('%s',exstr);
        try
            //jremove(e);
            //jremove(exstr);
            //jremove(ScilabSolver);
        catch 
        end        
        jinvoke(R.iserror,'set',%t);
        resultSet(R);
        errormessage = 'PAResult:PAwaitFor Internal Error while executing '+R.taskid;
    end    

    PAResult_clean(R);
    err = errormessage;
endfunction

function resultSet(R)
    if ~jinvoke(R.resultSet,'get') then

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
    end
endfunction

function printLogs(R,RaL,err)
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
        //jremove(logs,logstr);
    elseif err
        logstr = jinvoke(R.logs,'toString');
        if ~isempty(logstr) then
            printf('%s',logstr);
        end
    end    
endfunction