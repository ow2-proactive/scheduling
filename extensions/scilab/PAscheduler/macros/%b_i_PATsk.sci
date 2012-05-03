function a=%b_i_PATsk(i1,b,a)
    if i1 == 'Compose' then
        a.Compose=b;
    elseif i1 == 'Static' then
        a.Static=b; 
    else
        error('Type mismatch, '+i1+' doesn''t expects a boolean');
    end
endfunction
