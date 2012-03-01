
function res = PAgetDataspaceRegistry(varargin)
mlock
persistent registry
if nargin == 1
    registry = varargin{1};
elseif nargin ~= 0
        error('two many arguments');    
end
res = registry;