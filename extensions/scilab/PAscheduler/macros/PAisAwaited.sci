function tf=PAisAwaited(l)
    if typeof(l) == 'PAResL' then
        m = size(l.matrix,2);
        for i=1:m
            pares=l.matrix(i).entries;
            tf(i)=PAResult_PAisAwaited(pares);
        end
    else
        error('Expected argument of type PAResL, received '+typeof(l))
    end
endfunction