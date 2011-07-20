function tf=PAResult_PAisAwaited(l)
    jimport org.objectweb.proactive.api.PAFuture;
    tf=PAFuture.isAwaited(l.future);
endfunction