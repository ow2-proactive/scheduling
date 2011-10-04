function this=assign(this, attrib, value)
checkValidity(attrib,value);
switch attrib
    case 'Func'
        %% Code to check input arguments of given function
        %         if isa(value, 'function_handle')
        %             try
        %                 arginins = signature(value);
        %                 j=1;
        %                 argins = {};
        %                 for i=1:length(arginins)
        %                     if ~isnumeric(arginins{i})
        %                         argins{j} = arginins{i};
        %                         j=j+1;
        %                     end
        %                 end
        %             catch
        %                 argins = {};
        %             end
        %         else
        %             argins = {};
        %         end
        this.Func = value;
    case 'Params'
        if iscell(value)
            this.Params = value;
        elseif isnumeric(value) && isempty(value)
            this.Params = {};
        else
            this.Params = {value};
        end
    case 'InputFiles'
        if iscell(value) || (isnumeric(value) && isempty(value))
            this.InputFiles = value;
        else
            this.InputFiles = {value};
        end
    case 'OutputFiles'
        if iscell(value) || (isnumeric(value) && isempty(value))
            this.OutputFiles = value;
        else
            this.OutputFiles = {value};
        end
    case 'SelectionScript'
        if ischar(value)
            this.SelectionScript = ['file:' strrep(value, '\', '/')];
        else
            this.SelectionScript = value;
        end
    case 'Description'
        this.Description = value;
    case 'Compose'
        this.Compose = value;
    otherwise
        error(['unknown attribute :' attrib]);
end
% if ~this.Compose
%     % Check parameters assigment
%     np = length(this.Params);
%     if ~ismember('varargin', argins)
%         if length(argins) ~= np
%             warning('Number of parameters differs from function''s number of inputs');
%         end
%     end
% end
end