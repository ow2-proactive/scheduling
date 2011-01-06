function %_JArray_p(arr)
  s=invoke_u(arr,'toStrings');
  for i=s
    disp(i);
  end
endfunction