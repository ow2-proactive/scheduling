function res=PAResult(future,taskinfo)
    global ('PAResult_DB');
    if ~exists('PAResult_DB')
        PAResult_DB = list();
    end
    PAResult_DB($+1) = %f;
    rid = length(PAResult_DB);
    jimport java.util.concurrent.atomic.AtomicBoolean;
    jautoUnwrap(%t);             
    jimport java.lang.StringBuilder;
    res = tlist(['PAResult','future','cleanFileSet','cleanDirSet', 'outFile','jobid','taskid', 'cleaned', 'logsPrinted','logs','waited','iserror','resultSet','rid'],future, taskinfo.cleanFileSet, taskinfo.cleanDirSet, taskinfo.outFile, taskinfo.jobid, taskinfo.taskid, jnewInstance(AtomicBoolean,%f), jnewInstance(AtomicBoolean,%f), jnewInstance(StringBuilder), jnewInstance(AtomicBoolean,%f), jnewInstance(AtomicBoolean,%f),jnewInstance(AtomicBoolean,%f), rid);
    
endfunction







