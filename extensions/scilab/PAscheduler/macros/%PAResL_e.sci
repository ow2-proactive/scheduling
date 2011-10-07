function [b]=%PAResL_e(i1,i2,l)
    //disp('PAResL_e_ : '+string(i1)+' '+typeof(l))
    //[m, n] = size(l(2));
    //disp(i1);
    //disp(i2);
    //b = mlist(['PAResL','matrix']);
    //b.matrix = l.matrix(1,i1);

    if argn(2) == 3
        b = mlist(['PAResL','matrix']);
        b.matrix = l.matrix(i1,i2);
    elseif argn(2) == 2
        l=i2;
        if typeof(i1) == 'string' then
            if size(l.matrix,1) == 1 & size(l.matrix,2) == 1 then
                if typeof(i1) == 'string' then
                    out = %PAResult_e(i1,l.matrix(1,1).entries);
                    b=out;                
                else
                    b = mlist(['PAResL','matrix']);
                    b.matrix = l.matrix(i1);
                end            
            else            
                b=list();

                for j=1:size(l.matrix,2)
                    out = %PAResult_e(i1,l.matrix(1,j).entries);
                    //disp(out)
                    b($+1) = out;
                end                       
            end                
        else
            b = mlist(['PAResL','matrix']);
            b.matrix = l.matrix(1,i1);
        end     


    end
endfunction