function [b]=%PAResL_e(i1,l)
    //disp('PAResL_e_ : '+string(i1)+' '+typeof(l))
    //[m, n] = size(l(2));
    //disp(i1);
    b = mlist(['PAResL','matrix']);
    b.matrix = l.matrix(1,i1);
endfunction