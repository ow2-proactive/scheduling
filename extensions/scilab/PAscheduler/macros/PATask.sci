function res=PATask(l,c)
    res = mlist(['PATask','matrix']);
    res.matrix = cell(l,c);
    for m=1:l
        for n=1:c
            res.matrix(m,n).entries = PATsk();
        end   
    end    
endfunction