//This function loads a java class in Scilab. The class must be available in the classpath or be a native class. The path must be given : class("java.lang.String")
//The first option is used to wrap the returned class into a mlist and the second to say if the result of a java function must be unwrapped.
//For example, S=class("java.lang.String",%t,%t);s=S.new("Hello world !");s.length() returns an int32 corresponding to the length...

function wobj=class(name, isWrapped, isObjUnwrapped)
  nargs = argn(2);
  select nargs
  case 0 then
      error(msprintf(gettext("%s: Wrong number of input argument(s): %d to %d expected."),"class",1,3));
  case 1 then
      if type(name) <> 10 then
	  error(msprintf(gettext("%s: Wrong type for input argument #%d: String expected.\n"),"class",1));
      end
      isWrapped = %T;
      isObjUnwrapped = %F;
  case 2 then
      if type(name) <> 10 then
	  error(msprintf(gettext("%s: Wrong type for input argument #%d: String expected.\n"),"class",1));
      end
      if type(isWrapped) <> 4 then
	  error(msprintf(gettext("%s: Wrong type for input argument #%d: Boolean expected.\n"),"class",2));
      end
      isObjUnwrapped = %F;
  case 3 then
      if type(name) <> 10 then
	  error(msprintf(gettext("%s: Wrong type for input argument #%d: String expected.\n"),"class",1));
      end
      if type(isWrapped) <> 4 then
	  error(msprintf(gettext("%s: Wrong type for input argument #%d: Boolean expected.\n"),"class",2));
      end
      if type(isObjUnwrapped) <> 4 then
	  error(msprintf(gettext("%s: Wrong type for input argument #%d: Boolean expected.\n"),"class",3));
      end
  else
      error(msprintf(gettext("%s: Wrong number of input argument(s): %d to %d expected."),"class",1,3));
  end
  
  if ~isWrapped then
      wobj = loadClass(name);
      return;
  end
  if isObjUnwrapped then
      wobj = wrapInMlist_u(loadClass(name));
  else
      wobj = wrapInMlist(loadClass(name));
  end
endfunction
