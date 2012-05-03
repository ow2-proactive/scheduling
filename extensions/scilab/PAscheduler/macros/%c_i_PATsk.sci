function a=%c_i_PATsk(i1,b,a)
    if i1 == 'Description' then
        a.Description=b;
    elseif i1 == 'Func' then
        a.Func = b;
    elseif i1 == 'InputFiles' then
        a.InputFiles = list(b);
    elseif i1 == 'OutputFiles' then
        a.OutputFiles = list(b);
    elseif i1 == 'SelectionScript' then
        a.SelectionScript = b;
    elseif i1 == 'ScriptParams' then
        a.ScriptParams = b;
    elseif i1 == 'Sources' then
        a.Sources = list(b);
    elseif i1 == 'Topology' then
        deff ("y=ismember(a,l)","y=(or(a==l))","n");
        values = {'arbitrary', 'bestProximity', 'thresholdProximity', 'singleHost', 'singleHostExclusive', 'multipleHostsExclusive', 'differentHostsExclusive'};
        if type(b)==10 then
            if ~ismember(b,values) then
                error('Wrong value for topology : ' + b);
            end
            a.Topology = b;
        else
            a.Topology = [];
        end
    else
        error('Type mismatch, '+i1+' doesn''t expects a string');
    end
endfunction