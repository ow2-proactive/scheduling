// Wrap a java object into a mlist, for the moment only public and/or static, fields cannot be wrapped since we cannot overload affection made by dot operator.
 
function wobj=wrapInMlist(obj)
  meths = getMethods(obj);
  fields = getFields(obj);
  infos = getfield(1, obj);
  id = obj._id;
  sid = string(id);
  if typeof(obj) == '_JClass' then
      wobj = mlist([infos 'new' meths fields], id);
      deff('y=tmpmacro(varargin)', 'y=wrapInMlist(newInstance_l(int32(' + sid + '),varargin))');
      setfield(3, tmpmacro, wobj);
      clear('tmpmacro');
      dec = 3;
  else
      wobj = mlist([infos meths fields], id);
      dec = 2;
  end
  s = size(meths, 2);
  for i=1:s
      deff('y=tmpmacro(varargin)', 'y=wrapInMlist(invoke_l(int32(' + sid + '),''' + meths(i) + ''',varargin))');
      setfield(i + dec, tmpmacro, wobj);
      clear('tmpmacro');
  end
  ss = size(fields, 2);
  dec = dec + s;
  for i=1:ss
      str = sid + ',''' + fields(i);
      deff('y=tmpmacro(x)', 'nargs=argn(2),if nargs>=2 then,error(''aa''),end,if nargs==0 then,y=wrapInMlist(getField_l(+' + str + ''')),else,setField_l(' + str + ''',x),abort,end');
      setfield(i + dec, tmpmacro, wobj);
      clear('tmpmacro');
  end 
endfunction
