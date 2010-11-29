function display(P)

s = size(P);
for i = 1:s(1)
    for j = 1:s(2)
        dp(P(i,j), inputname(1), i, j)
    end
end

end

function dp(X,name,k,l)
if isequal(get(0,'FormatSpacing'),'compact')
    if length(name) > 0
        disp([name '(' num2str(k) ',' num2str(l) ')' ' =']);
    end
    dp2(X.Func,'Func')
    dp2(X.Params,'Params')
    dp2(X.Description,'Description')
    dp2(X.InputFiles,'InputFiles')
    dp2(X.OutputFiles,'OuputFiles')
    dp2(X.Compose,'Compose')
    dp2(X.SelectionScript,'SelectionScript')
else
    disp(' ')
    if length(name) > 0
        disp([name '(' num2str(k) ',' num2str(l) ')' ' =']);
        disp(' ');
    end
    dp2(X.Func,'Func')
    dp2(X.Params,'Params')
    dp2(X.Description,'Description')
    dp2(X.InputFiles,'InputFiles')
    dp2(X.OutputFiles,'OuputFiles')
    dp2(X.Compose,'Compose')
    dp2(X.SelectionScript,'SelectionScript')
end
end

function dp2(Y,name)
if isnumeric(Y) && isempty(Y)
    return;
end
spacing=get(0,'FormatSpacing');
format compact
if iscell(Y)
    T = evalc('disp(Y);');
    disp(['    ' name ': ' strtrim(T)]);
elseif isa(Y,'function_handle')
    disp(['    ' name ':       @' char(Y)]);
elseif islogical(Y)
    if Y
        disp(['    ' name ':       true']);    
    else
        disp(['    ' name ':       false']);
    end
else
    try 
    disp(['    ' name ': ' char(Y)]);
catch
    disp(['    ' name ': ']);
    disp(Y);
end
    
end

set(0,'FormatSpacing',spacing);
end
