function val=PAgetLogs(l)
    if typeof(l) == 'PAResL' then
        tf = PAisAwaited(l);
        if and(~tf) then
            m = size(l.matrix,2);
        if m == 1
            pares=l.matrix.entries;
            val=jinvoke(pares.logs,'toString');
        else
            val=list();
            for i=1:m
                pares=l.matrix(i).entries;
                val($+1)=jinvoke(pares.logs,'toString');
            end
        end
        else
            error('All results must be available before retrieving logs, use PAwaitFor before');
        end        
    else
        error('Expected argument of type PAResL, received '+typeof(l))
    end
endfunction
