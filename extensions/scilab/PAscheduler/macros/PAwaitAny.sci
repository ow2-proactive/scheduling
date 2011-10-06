function val=PAwaitAny(l,timeout)
    if typeof(l) == 'PAResL' then 
        jimport org.objectweb.proactive.api.PAFuture;
        jimport java.util.ArrayList;
        
        arrayList = jnewInstance(ArrayList);
        indList=list();    
        m = size(l.matrix,2);
        for i=1:m
            pares=l.matrix(i).entries;
            if ~jexists(pares.waited) then
                error('PAResult::object cleared');
            end
            if ~jinvoke(pares.waited,'get')
                jinvoke(arrayList,'add',pares.future);
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
            ind = PAFuture.waitForAny(arrayList);
        else
            ind = PAFuture.waitForAny(arrayList);
        end
        jremove(arrayList,ArrayList,PAFuture);
        j=indList(ind+1);
        pares=l.matrix(j).entries;
        jinvoke(pares.waited,'set',%t);
        val = PAResult_PAwaitFor(pares);
    else
        val = [];
        error('Expected argument of type PAResL, received '+typeof(l))
    end
endfunction