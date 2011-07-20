function %PAResL_p(l)
    //disp('PAResL_p : '+typeof(l))
    if typeof(l) == 'PAResL' then
        m = size(l.matrix,2);
        //disp(m)
        for i=1:m
            pares=l.matrix(i).entries;
            printf('('+string(i)+'):\n');
            %PAResult_p(pares);
        end
    elseif typeof(l) == 'PAResult' then
        %PAResult_p(l);
    end

endfunction