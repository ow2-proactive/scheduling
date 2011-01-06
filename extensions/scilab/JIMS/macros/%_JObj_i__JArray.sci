function a=%_JObj_i__JArray(varargin)
  a=varargin($);
  b=varargin($ - 1);
  s=size(varargin)-2;
  arr=zeros(1,s);
  for i=1:s
    arr(i)=varargin(i);
  end
  b=javaCast(b,'java.lang.Object');
  invoke(a,'set',arr,b);
  remove(b);
endfunction