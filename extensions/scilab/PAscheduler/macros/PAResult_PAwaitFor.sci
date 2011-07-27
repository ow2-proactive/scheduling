function val=PAResult_PAwaitFor(R,timeout)
    global ('PAResult_DB');
    //disp('PAResult_PAwaitFor : init')
    jimport org.objectweb.proactive.api.PAFuture;
    errormessage=[];
    ff = R.future;
    if argn(2) == 2
        
        RaL = PAFuture.getFutureValue(ff,timeout);
    else
        RaL = PAFuture.getFutureValue(ff);
    end
    if jinvoke(RaL,'isOK')
        //disp('PAResult_PAwaitFor : isOK')
        if ~jinvoke(R.logsPrinted,'get')
            logs = jinvoke(RaL,'getLogs');
            jinvoke(R.logs,'append',logs);
            jinvoke(R.logsPrinted,'set',%t);
            if ~isempty(logs) then
                printf('%s',logs);
            end
        end

        if jinvoke(R.resultSet,'get')
            //disp('PAResult_PAwaitFor : Result get')
            val=PAResult_DB(R.rid);
            //disp(val)
        else
            //disp('PAResult_PAwaitFor : Result set')
            load(R.outFile);
            val = out;
            PAResult_DB(R.rid) = out;
            //disp(val)
            jinvoke(R.resultSet,'set',%t);
        end

    elseif jinvoke(RaL,'isMatSciError');
        //disp('PAResult_PAwaitFor : isMatSciError')
        if ~jinvoke(R.logsPrinted,'get')
            logs = jinvoke(RaL,'getLogs');
            jinvoke(R.logs,'append',logs);
            jinvoke(R.logsPrinted,'set',%t);
            if ~isempty(logs) then
                printf('%s',logs);
            end
            //System.err.println(logs);
        end
        jinvoke(R.iserror,'set',%t);
        errormessage = 'PAResult:PAwaitFor Error during remote script execution';
    else
        //disp('PAResult_PAwaitFor : internalError')
        if ~jinvoke(R.logsPrinted,'get')
            logs = jinvoke(RaL,'getLogs');
            if ~isempty(logs) then
                jinvoke(R.logs,'append',logs);
                jinvoke(R.logsPrinted,'set',%t);
                printf('%s',logs);
            end

        end
        e = jinvoke(RaL,'getException');
        jimport org.ow2.proactive.scheduler.ext.scilab.client.ScilabSolver;
        exstr = ScilabSolver.getStackTrace(e);
        printf('%s',exstr);
        //err = java.lang.System.err;
        //jinvoke(e.printStackTrace(err);
        jinvoke(R.iserror,'set',%t);
        errormessage = 'PAResult:PAwaitFor Internal Error';
    end
    PAResult_clean(R);
    if errormessage ~= [] then
        val = [];
        error(errormessage)
    end
endfunction