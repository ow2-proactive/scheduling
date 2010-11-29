function obj = dummy(varargin)

% Constructor for a dummy class object.
% You must always pass one argument if you want to create a new object.

if nargin==0 % Used when objects are loaded from disk
  obj = init_fields;
  obj = class(obj, 'dummy');
  return;
end
firstArg = varargin{1};
if isa(firstArg, 'dummy') %  used when objects are passed as arguments
  obj = firstArg;
  return;
end

% We must always construct the fields in the same order,
% whether the object is new or loaded from disk.
% Hence we call init_fields to do this.
obj = init_fields; 

% attach class name tag, so we can call member functions to
% do any initial setup
obj = class(obj, 'dummy'); 

% Now the real initialization begins
obj.field1 = rand(2,3);
obj.field2 = varargin{1};


%%%%%%%%% 

function obj = init_fields()
% Initialize all fields to dummy values 
obj.field1 = [];
obj.field2 = [];
