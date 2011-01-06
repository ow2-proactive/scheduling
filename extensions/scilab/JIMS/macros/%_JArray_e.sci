function y=%_JArray_e(varargin)
  M=varargin($);
  s=size(varargin)-1;
  arr=zeros(1,s);
  for i=1:s
    arr(i)=varargin(i);
  end
  y=wrapInMlist(invoke(M,'get',arr));
endfunction



  
  