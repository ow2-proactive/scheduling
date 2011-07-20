function res=PAResL(pares)
    res = mlist(['PAResL','matrix']);
    if typeof(pares) == 'list'      
        //disp(length(pares))
        mat = cell(1,length(pares));
        for i=1:length(pares)
            mat(i).entries = pares(i);
        end        
        res.matrix= mat;
        
    elseif typeof(pares) == 'cell'
        res.matrix = pares;
    else
        res.matrix = cell(1,1);
        res.matrix.entries = pares;        
    end             

endfunction







