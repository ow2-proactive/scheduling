function a=%PATask_i_PATask(i1,i2,b,a)
    if argn(2) == 4         
        a.matrix(i1,i2) = b.matrix;
    else
        error('Two many dimensions');
            
    end      
endfunction