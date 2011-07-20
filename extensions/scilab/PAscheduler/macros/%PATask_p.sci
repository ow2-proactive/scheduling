function %PATask_p(l)
    if typeof(l) == 'PATask' then
        m = size(l.matrix,1);
        n = size(l.matrix,2);
        //disp(m)
        for i=1:m
            for j=1:n
                patsk=l.matrix(i,j).entries;
                printf('('+string(i)+','+string(j)+'):\n');
                %PATsk_p(patsk);
            end
            
        end
     end
endfunction