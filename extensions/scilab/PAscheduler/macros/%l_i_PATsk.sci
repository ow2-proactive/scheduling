function a=%l_i_PATsk(i1,b,a)
    if i1 == 'Params' then
        a.Params=b;    
    elseif i1 == 'InputFiles' then
        a.InputFiles = b;
    elseif i1 == 'OutputFiles' then
        a.OutputFiles = b;    
    elseif i1 == 'Sources' then
        a.Sources = b;   
    else
        error('Type mismatch, '+i1+' doesn''t expects a string');
    end
endfunction