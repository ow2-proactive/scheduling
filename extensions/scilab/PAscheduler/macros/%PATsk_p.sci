function %PATsk_p(l)
    printf('Func: '+l.Func+'\n');
    printf('Params:\n');
    for i=1:length(l.Params)
        disp(l.Params(i));
    end
    if ~isempty(l.Description) then
        printf('Description: '+l.Description+'\n');
    end
    if ~isempty(l.InputFiles) then        
        printf('InputFiles: ');
        for i=1:length(l.InputFiles)
            printf('%s ', l.InputFiles(i));
        end 
        printf('\n');               
    end
    if ~isempty(l.InputSource) then        
        printf('InputSource: '+l.InputSource+'\n');                   
    end
    if ~isempty(l.OutputFiles) then
        printf('OutputFiles: ');
        for i=1:length(l.OutputFiles)
            printf('%s ', l.OutputFiles(i));
        end 
        printf('\n');
    end
    if ~isempty(l.OutputSource) then        
        printf('OutputSource: '+l.OutputSource+'\n');                   
    end
    if ~isempty(l.SelectionScript) then
        printf('SelectionScript: '+l.SelectionScript+'\n');
    end
    if l.Static then
        printf('Static: true\n');
    else
        printf('Static: false\n');
    end
    if ~isempty(l.ScriptParams) then
        printf('ScriptParams: '+l.ScriptParams+'\n');        
    end
    if (l.NbNodes > 1) then
        printf('NbNodes: '+string(l.NbNodes)+'\n');        
    end
    if ~isempty(l.Topology) then
        printf('Topology: '+l.Topology+'\n');        
    end
    if ~isempty(l.ThresholdProximity) then
        printf('ThresholdProximity: '+string(l.ThresholdProximity)+'\n');        
    end
    if ~isempty(l.Sources) then
        printf('Sources: ');
        for i=1:length(l.Sources)
            printf('%s ', l.Sources(i));
        end 
        printf('\n');        
    end
    if l.Compose then
        printf('Compose: true\n');
    else
        printf('Compose: false\n');
    end
    printf('\n');

endfunction
