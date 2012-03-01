function res=PAResult(taskinfo)
    global ('PAResult_DB');
    if typeof(PAResult_DB) ~= 'list'
        PAResult_DB = list();
    end    
    PAResult_DB($+1) = %f;
    dbrid = length(PAResult_DB);  
    jimport java.util.concurrent.atomic.AtomicBoolean;
    jimport org.ow2.proactive.scheduler.ext.matsci.client.common.data.UnReifiable;                 
    jimport java.lang.StringBuilder;
    res = tlist(['PAResult','cleanFileSet','cleanDirSet', 'outFile','jobid','taskid', 'cleaned', 'logsPrinted','logs','waited','iserror','resultSet','dbrid','sid','RaL'],taskinfo.cleanFileSet, taskinfo.cleanDirSet, taskinfo.outFile, taskinfo.jobid, taskinfo.taskid, jnewInstance(AtomicBoolean,%f), jnewInstance(AtomicBoolean,%f), jnewInstance(StringBuilder), jnewInstance(AtomicBoolean,%f), jnewInstance(AtomicBoolean,%f),jnewInstance(AtomicBoolean,%f), dbrid, taskinfo.sid, jnewInstance(UnReifiable));
    //jremove(AtomicBoolean, StringBuilder);
    
endfunction







