function %PAResL_p(l)
    //disp('PAResL_p : '+typeof(l))
     
    if typeof(l) == 'PAResL' then
        tf=PAisAwaited(l);
        m = size(l.matrix,2);
        fd = find(tf == %f);
        if length(fd) > 0 then
            PAwaitFor(l(fd)); 
        end               
        //disp(m)
        for i=1:m            
            R=l.matrix(i).entries;
            printf('('+string(i)+'):\n');
            if tf(i) then
                printf('Awaited (J:'+ string(R.jobid)+ ')\n');
            else
                disp(PAResult_PAwaitFor(R));
            end
                        
        end
    elseif typeof(l) == 'PAResult' then
        %PAResult_p(l);
    end

endfunction