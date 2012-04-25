function a=%s_i_PATsk(i1,b,a)
    if i1 == 'NbNodes' then
        if or(type(b)==[1 5 8]) & sum(length(b))==1 & (round(b) == b) & b >= 1 then
            a.NbNodes=b;
        else 
            error('NbNodes must be an integer >= 1');
        end
    elseif i1 == 'InputFiles' then
        if isempty(b) then
            a.InputFiles = list();
        else
            error('InputFiles must be a list, a string or null');
        end    
     elseif i1 == 'OutputFiles' then
        if isempty(b) then
            a.OutputFiles = list();
        else
            error('OutputFiles must be a list, a string or null');
        end    
     elseif i1 == 'Sources' then
        if isempty(b) then
            a.Sources = list();
        else
            error('Sources must be a list, a string or null');
        end
    elseif i1 == 'ThresholdProximity' then
        if or(type(b)==[1 5 8]) & sum(length(b))==1 & (round(b) == b) & b >= 0 then
            a.ThresholdProximity=b;
        else 
            error('ThresholdProximity must be a positive integer');
        end
        
    elseif i1 == 'SelectionScript' then
        if isempty(b) then
            a.SelectionScript = b;
        else
            error('SelectionScript must be a string or null');
        end
    elseif i1 == 'ScriptParams' then
        if isempty(b) then
            a.ScriptParams = b;
        else
            error('ScriptParams must be a string or null');
        end
    elseif i1 == 'Description' then
        if isempty(b) then
            a.Description = b;
        else
            error('Description must be a string or null');
        end
    elseif i1 == 'Topology' then
        if isempty(b) then
            a.Topology = b;
        else
            error('Topology must be a string or null');
        end    
    else
        error('Type mismatch, '+i1+' doesn''t expects a numeric constant');
    end
endfunction