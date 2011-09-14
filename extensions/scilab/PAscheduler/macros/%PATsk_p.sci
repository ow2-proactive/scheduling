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
    if ~isempty(l.OutputFiles) then
        printf('OutputFiles: ');
        for i=1:length(l.OutputFiles)
            printf('%s ', l.OutputFiles(i));
        end 
        printf('\n');
    end
    if ~isempty(l.SelectionScript) then
        printf('SelectionScript: '+l.SelectionScript+'\n');
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