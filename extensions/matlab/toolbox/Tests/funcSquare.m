function out=funcSquare(in)
disp(in);
% if 
if ischar(in)
    error('char argument not expected');
end
out=in*in;
end