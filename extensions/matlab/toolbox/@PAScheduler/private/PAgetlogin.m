function res = PAgetlogin(varargin)
mlock
persistent login
if nargin == 1
    login = varargin{1};
elseif nargin ~= 0
        error('two many arguments');    
end
res = login;