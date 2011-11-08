function tf=PAResult_PAisAwaited(l)    
    if ~jexists(l.future) then
        error('PAResult::object cleared');
    end
    jimport org.objectweb.proactive.api.PAFuture;
    tf=PAFuture.isAwaited(l.future);
    //jremove(PAFuture);
endfunction