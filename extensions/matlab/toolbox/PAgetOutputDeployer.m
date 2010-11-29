function varargout = PAgetOutputDeployer(varargin)
mlock
persistent deployers
if exist('deployers','var') == 1 && isa(deployers,'java.util.HashMap')
else
   deployers = java.util.HashMap(); 
end
if nargin == 2
    deployers.put(varargin{1}, varargin{2});    
elseif nargin == 1
    varargout{1} = deployers.get(varargin{1});
    
else
    error('Wrong number of arguments');
end
