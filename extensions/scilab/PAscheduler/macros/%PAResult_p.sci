function %PAResult_p(R)
    //disp('PAResult_p : '+typeof(l))
    if typeof(R) == 'PAResult' then
        if PAResult_PAisAwaited(R)
            printf('Awaited (J:'+ string(R.jobid)+ ')\n')
        else
            //try
                disp(PAResult_PAwaitFor(R));
            //catch
                // error ignored in display
            //end
        end
    elseif typeof(R) == 'PAResL' then
        %PAResL_p(R);
    end

endfunction