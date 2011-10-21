function sz=%PAResL_size(l,i)
    if argn(2) == 2 then
        sz=size(l.matrix,i);
    else
        sz=size(l.matrix);
    end
endfunction