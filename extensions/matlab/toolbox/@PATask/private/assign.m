function this=assign(this, varargin)
if (mod(length(varargin),2) ~= 0) && (length(varargin) ~= 1)
    error(['Invalid number of arguments : ' num2str(length(varargin))]);
end
if ~isscalar(this)
    error(['Invalid size of this : ' num2str(size(this))]);
end
if strcmp(class(varargin{1}), 'PATask')
    input = varargin{1};
else
    for i=1:2:length(varargin)
        attrib = varargin{i};
        value = varargin{i+1};
        checkValidity(attrib,value);
        input.(attrib) = value;
    end
end
fn = fieldnames(input);
for i=1:length(fn)
    attrib = fn{i};
    value = input.(fn{i});    
    switch attrib
        case 'Func'
            % Code to check input arguments of given function
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
        case 'NbNodes'
            this.NbNodes = value;
        case 'Topology'
            this.Topology = value;  
        case 'ThresholdProximity'
            this.ThresholdProximity = value; 
        case 'id'
            this.id = value;
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
end