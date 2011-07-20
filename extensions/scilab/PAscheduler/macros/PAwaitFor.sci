function val=PAwaitFor(l,timeout)
    if typeof(l) == 'PAResL' then
        m = size(l.matrix,2);
        if m == 1
            pares=l.matrix.entries;
            if argn(2) == 2
                val=PAResult_PAwaitFor(pares,timeout);
            else
                val=PAResult_PAwaitFor(pares);
            end
        else
            val=list();
            for i=1:m
                pares=l.matrix(i).entries;
                if argn(2) == 2
                    val($+1)=PAResult_PAwaitFor(pares,timeout);
                else
                    val($+1)=PAResult_PAwaitFor(pares);
                end
            end
        end
    else
        val = [];
        error('Expected argument of type PAResL, received '+typeof(l))
    end
endfunction