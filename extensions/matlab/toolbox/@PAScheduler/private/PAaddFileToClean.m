function varargout = PAaddFileToClean(varargin)
mlock
persistent filesperjob
if exist('filesperjob','var') == 1 && isstruct(filesperjob)
else
    filesperjob.a = [];
end
if nargin == 2
    key = ['j' char(varargin{1})];
    if ~isfield(filesperjob, key)
        if iscell(varargin{2})
            filesperjob.(key) = varargin{2};
        else
            filesperjob.(key) = {varargin{2}};
        end
    else
        if iscell(varargin{2})
            filesperjob.(key) = union(filesperjob.(key),varargin{2});
        else
            filesperjob.(key) = union(filesperjob.(key),{varargin{2}});
        end
    end    
elseif nargin == 1
    key = ['j' char(varargin{1})];
    try
        files = filesperjob.(key);
        warning('off')
        for i=1:length(files)
            try
                delete(files{i});
            catch
            end
        end
        warning('on')
    catch
    end
else
    error('Wrong number of arguments');
end