n = fix(length(in)/nout);
for i = 1:nout-1    
    out(i)= {in(1+n*(i-1):n*i)};
end
out(nout)= {in(1+n*(nout-1):end)};
    