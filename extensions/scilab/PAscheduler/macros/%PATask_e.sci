function [b]=%PATask_e(i1,i2,l)
    //disp('PAResL_e_ : '+string(i1)+' '+typeof(l))
    //[m, n] = size(l(2));
    //disp(i1);

    if argn(2) == 3
        b = mlist(['PATask','matrix']);
        b.matrix = l.matrix(i1,i2);
    elseif argn(2) == 2
        l=i2;
        if size(l.matrix,1) == 1 & size(l.matrix,2) == 1 then
            out = %PATsk_e(i1,l.matrix(1,1).entries);
            b=out;
        elseif size(l.matrix,1) == 1

            b=list();
            for j=1:size(l.matrix,2)
                out = %PATsk_e(i1,l.matrix(1,j).entries);
                //disp(out)
                b($+1) = out;
            end

        elseif size(l.matrix,2) == 1

            b=list();
            for j=1:size(l.matrix,1)
                out = %PATsk_e(i1,l.matrix(j,1).entries);
                //disp(out)
                b($+1) = out;
            end

        else
            error('Two many dimensions');

        end
    end
endfunction