
for i=1:n,
  for j=1:n, 
    if i==j then
      x(i,j)=2;
    else x(i,j)=4;
    end;
  end;
end;

y=tril(x);
