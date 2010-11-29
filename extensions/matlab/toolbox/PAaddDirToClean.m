function varargout = PAaddDirToClean(varargin)
mlock
persistent dirs
if exist('dirs','var') == 1 && iscellstr(dirs)
else
   dirs = {}; 
end
if nargin == 1
    dirs = union(dirs, {varargin{1}});
else
    warning('off')
    for i=1:length(dirs)
        try
            rmdir(dirs{i});
        catch
        end
    end
    warning('on')
end