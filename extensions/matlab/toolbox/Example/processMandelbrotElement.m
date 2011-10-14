function count = processMandelbrotElement(x,y,maxIterations)
n=numel(x);
n2=numel(y);
if n ~=n2
    error('Dimension mismatch');
end
for i=1:n
    x0=x(i);
    y0=y(i);
    z0 = complex(x0,y0);
    z = z0;
    cnt = 1;
    while (cnt <= maxIterations) ...
         && ((real(z)*real(z) + imag(z)*imag(z)) <= 4)
    cnt = cnt + 1;
    z = z*z + z0;
end
count(i) = log(cnt);
end
count = reshape(count, size(x));