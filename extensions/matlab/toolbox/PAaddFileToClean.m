function varargout = PAaddFileToClean(varargin)
mlock
persistent files
if exist('files','var') == 1 && iscellstr(files)
else
   files = {}; 
end
if nargin == 1
    files = union(files, {varargin{1}});
else
    warning('off')
    for i=1:length(files)
        try
            delete(files{i});
        catch
        end
    end
    warning('on')
end