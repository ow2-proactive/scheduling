function res=PAResult(future,taskinfo)
    global ('PAResult_DB');
    if typeof(PAResult_DB) ~= 'list'
        PAResult_DB = list();
    end    
    PAResult_DB($+1) = %f;
    dbrid = length(PAResult_DB);  
    jimport java.util.concurrent.atomic.AtomicBoolean;
    jautoUnwrap(%t);             
    jimport java.lang.StringBuilder;
    res = tlist(['PAResult','future','cleanFileSet','cleanDirSet', 'outFile','jobid','taskid', 'cleaned', 'logsPrinted','logs','waited','iserror','resultSet','dbrid','sid'],future, taskinfo.cleanFileSet, taskinfo.cleanDirSet, taskinfo.outFile, taskinfo.jobid, taskinfo.taskid, jnewInstance(AtomicBoolean,%f), jnewInstance(AtomicBoolean,%f), jnewInstance(StringBuilder), jnewInstance(AtomicBoolean,%f), jnewInstance(AtomicBoolean,%f),jnewInstance(AtomicBoolean,%f), dbrid, taskinfo.sid);
    jremove(AtomicBoolean, StringBuilder);
    
endfunction







