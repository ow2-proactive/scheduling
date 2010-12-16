function varargout = PAaddDirToClean(varargin)
mlock
persistent dirsperjob
if exist('dirsperjob','var') == 1 && isstruct(dirsperjob)
else
    dirsperjob.a = [];
end
if nargin == 2
    key = ['j' char(varargin{1})];
    if ~isfield(dirsperjob, key)
        if iscell(varargin{2})
            dirsperjob.(key) = varargin{2};
        else
            dirsperjob.(key) = {varargin{2}};
        end
    else
        if iscell(varargin{2})
            dirsperjob.(key) = union(dirsperjob.(key),varargin{2});
        else
            dirsperjob.(key) = union(dirsperjob.(key),{varargin{2}});
        end
    end
elseif nargin == 1
    key = ['j' char(varargin{1})];
    if isfield(dirsperjob, key)
        dirs = dirsperjob.(key);
        warning('off')
        for i=1:length(dirs)
            try
                rmdir(dirs{i});
            catch
            end
        end
        warning('on')
    end
else
    error('Wrong number of arguments');
end