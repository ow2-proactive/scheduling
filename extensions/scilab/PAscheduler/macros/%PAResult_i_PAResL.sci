function a=%PAResult_i_PAResL(i1,b,a)
    //[m, n] = size(l(2));
    //disp('%PAResult_i_PAResL : '+string(i1)+' '+typeof(b)+' '+typeof(a))
    if length(i1) > 1 then
        error('scalar index expected, received :'+string(i1));
    end
    a.matrix(1,i1).entries = b;
endfunction