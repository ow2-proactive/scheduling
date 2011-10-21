function a=%s_i_PATsk(i1,b,a)
    if i1 == 'NbNodes' then
        if or(type(b)==[1 5 8]) & sum(length(b))==1 & (round(b) == b) & b >= 1 then
            a.NbNodes=b;
        else 
            error('NbNodes must be an integer >= 1');
        end
        
    elseif i1 == 'ThresholdProximity' then
        if or(type(b)==[1 5 8]) & sum(length(b))==1 & (round(b) == b) & b >= 0 then
            a.ThresholdProximity=b;
        else 
            error('ThresholdProximity must be a positive integer');
        end
        
    else
        error('Type mismatch, '+i1+' doesn''t expects a numeric constant');
    end
endfunction