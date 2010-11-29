function varargout=checkValidity(attrib, val)
switch attrib    
    case 'Func'
        if ~(isa(val, 'function_handle') || (isnumeric(val) && isempty(val)))
            error('Func must be a function handle');
        end
    case 'Description'
        if ~(ischar(val) || (isnumeric(val) && isempty(val)))
            error('Description must be a string');
        end
    case 'InputFiles'
        if ~(iscellstr(val) || (isnumeric(val) && isempty(val)))
            error('InputFiles must be a cell array of strings');
        end
    case 'OutputFiles'
        if ~(iscellstr(val) || (isnumeric(val) && isempty(val)))
            error('OutputFiles must be a cell array of strings');
        end    
    case 'Compose'
        if ~(islogical(val) || (isnumeric(val) && isempty(val)))
            error('Compose must be logical');
        end  
    case 'SelectionScript'
        if ~(ischar(val) || (isnumeric(val) && isempty(val)))
            error('SelectionScript must be a string');
        end
end
