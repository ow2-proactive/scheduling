function a=%PAResL_i_PAResL(i1,b,a)
    //[m, n] = size(l(2));
    //disp('%PAResL_i_PAResL : '+string(i1)+' '+typeof(b)+' '+typeof(a))
    a.matrix(1,i1) = b.matrix;    
endfunction