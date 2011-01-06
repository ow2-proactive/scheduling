// The same as wrapInMlist, but when the resulting object can be unwrapped (like a double or an int), it's unwrapped.

function wobj=wrapInMlist_u(obj)
  if typeof(obj) <> '_JObj' & typeof(obj) <> '_JClass' then
      wobj = obj;
      return;
  end
  meths = getMethods(obj);
  fields = getFields(obj);
  infos = getfield(1, obj);
  id = obj._id;
  sid = string(id);
  if typeof(obj) == '_JClass' then
      wobj = mlist([infos 'new' meths fields], id);
      deff('y=tmpmacro(varargin)', 'y=wrapInMlist_u(newInstance_l(int32(' + sid + '),varargin))');
      setfield(3, tmpmacro, wobj);
      clear('tmpmacro');
      dec = 3;
  else
      wobj = mlist([infos meths fields], id);
      dec = 2;
  end
  s = size(meths, 2);
  for i=1:s
      deff('y=tmpmacro(varargin)', 'y=wrapInMlist_u(invoke_lu(int32(' + sid + '),''' + meths(i) + ''',varargin))');
      setfield(i + dec, tmpmacro, wobj);
      clear('tmpmacro');
  end
  ss = size(fields, 2);
  dec = dec + s;
  for i=1:ss
      str = sid + ',''' + fields(i);
      deff('y=tmpmacro(x)', 'nargs=argn(2),if nargs>=2 then,error(''aa''),end,if nargs==0 then,y=wrapInMlist_u(getField_lu(+' + str + ''')),else,setField_l(' + str + ''',x),abort,end');
      setfield(i + dec, tmpmacro, wobj);
      clear('tmpmacro');
  end 
endfunction
