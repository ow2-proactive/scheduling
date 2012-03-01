
function res = PAgetJVMInterface(varargin)
mlock
persistent jvmint
if nargin == 1
    jvmint = varargin{1};
elseif nargin ~= 0
        error('two many arguments');    
end
res = jvmint;