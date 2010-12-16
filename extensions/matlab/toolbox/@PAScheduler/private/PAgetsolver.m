%   PAgetSolver() - sets or returns the connection to the ProActive scheduler
%
%   Usage:
%       >> solver = PAgetsolver();
%       >> PAgetsolver(solver);
%
%   Inputs:
%       solver - connection to the ProActive Scheduler (in this case the function stores it)
%
%   Ouputs:
%       solver - stored connection to the ProActive Scheduler
%

function res = PAgetsolver(varargin)
mlock
persistent solver
if nargin == 1
    solver = varargin{1};
elseif nargin ~= 0
        error('two many arguments');    
end
res = solver;
