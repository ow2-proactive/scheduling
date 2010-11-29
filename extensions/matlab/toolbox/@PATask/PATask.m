function varargout = PATask(varargin)
persistent lastid
if exist('lastid','var') == 1 && isa(lastid,'int64')
    lastid=int64(double(lastid)+1);
else
    lastid = int64(0);
end
if nargin > 0
    checkValidity('Func',varargin{1});
    this.Func = varargin{1};
    if isa(this.Func, 'function_handle')
        try 
            arginins = signature(this.Func);
            j=1;
            argins = {};
            for i=1:length(arginins)
                if ~isnumeric(arginins{i})
                    argins{j} = arginins{i};
                    j=j+1;
                end
            end
        catch
            argins = {};
        end
    else
        argins = {};
    end
    if iscell(varargin{2})
        this.Params = varargin{2};
    elseif isnumeric(varargin{2}) && isempty(varargin{2})
        this.Params = {};
    else
        this.Params = {varargin{2}};
    end
    %checkValidity('Out',varargin{3});
    %this.Out = varargin{3};
    checkValidity('Description',varargin{3});
    this.Description = varargin{3};
    checkValidity('InputFiles',varargin{4});
    this.InputFiles = varargin{4};
    checkValidity('OutputFiles',varargin{5});
    this.OutputFiles = varargin{5};
    checkValidity('Compose',varargin{6});
    this.Compose=varargin{6};
    checkValidity('SelectionScript', varargin{7})
    this.SelectionScript = varargin{7};
    if ischar(this.SelectionScript)
        this.SelectionScript = ['file:' strrep(this.SelectionScript, '\', '/')];
    end
    if ~this.Compose
        % Check parameters assigment
        np = length(this.Params);
        if ~ismember('varargin', argins)
            if length(argins) ~= np
                warning('Number of parameters differs from function''s number of inputs');
            end
        end
    end
    this.id = lastid;
else
    this.Func = [];
    this.Params = {};
    this.Description = [];
    this.InputFiles = [];
    this.OutputFiles = [];
    this.Compose=false;
    this.SelectionScript = [];
    this.id = lastid;
end
for i=1:nargout
    varargout{i}=[];
    varargout{i} = class(this,'PATask');
end

