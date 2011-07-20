function b=%PATask_6(i1,i2,a)
    //if size(a.matrix,1)*size(a.matrix,2) > 1 then
    //    error('Matrix too big');
    //end
//    disp(argn(2))
//    disp('i1='+typeof(i1))
//    disp(i1)
//    disp('i2='+typeof(i2))
//    disp(i2)    
//    disp('a='+typeof(a))
//    disp(a)    
    b = mlist(['PATask','matrix']);
    b.matrix = a.matrix(i1,i2);
endfunction