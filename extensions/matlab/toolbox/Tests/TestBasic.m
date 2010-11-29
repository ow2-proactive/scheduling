function [ok, msg]=TestBasic(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 200000;
    else
        timeout = 80000;
    end
end

disp('...... Testing PAsolve with factorial');
disp('..........................1 PAwaitAll');
resl = PAsolve(@factorial,{1},{2},{3},{4},{5});
val=PAwaitAll(resl,timeout)
[ok,msg]=checkValuesFact(val);
if ~ok disp(msg),return; end
disp('..........................1 ......OK');
clear val;

disp('..........................2 PAwaitAny');
resl = PAsolve(@factorial,{1},{2},{3},{4},{5});
for i=1:5
    val(i)=PAwaitAny(resl,timeout)
end
val=sort(val)
[ok,msg]=checkValuesFact(val);
if ~ok disp(msg),return; end
disp('..........................2 ......OK');
clear val;

disp('..........................3 PAwaitFor');
resl = PAsolve(@factorial,{1},{2},{3},{4},{5});
for i=1:5
    val(i)=PAwaitFor(resl(i),timeout)
end
[ok,msg]=checkValuesFact(val);
if ~ok disp(msg),return; end
disp('..........................3 ......OK');
clear val;

disp('..........................4 global val attribute');
resl = PAsolve(@factorial,{1},{2},{3},{4},{5});
val=resl(1:5).val
[ok,msg]=checkValuesFact(val);
if ~ok disp(msg),return; end
disp('..........................4 ......OK');
clear val;

disp('..........................5 val and logs attributes');
resl = PAsolve(@factorial,{1},{2},{3},{4},{5});
for i=1:5
    val{i}=resl(i).val;
    logs{i}=resl(i).logs;
end
[ok,msg]=checkValuesFact(val);
if ~ok disp(msg),return; end
disp('..........................5 ......OK');
clear val;
clear logs;

disp('...... Testing PAsolve with funcSquare and an error');
disp('..........................6 PAwaitAll');
resl = PAsolve(@funcSquare,{1},{2},{3},{'a'},{5});
val=PAwaitAll(resl,timeout)
[ok,msg]=checkValuesSquare(val);
if ~ok disp(msg),return; end
disp('..........................6 ......OK');
clear val;

disp('..........................7 PAwaitAny');
resl = PAsolve(@funcSquare,{1},{2},{3},{'a'},{5});
j=1;
for i=1:5
    try
        val(j)=PAwaitAny(resl,timeout)
        j=j+1;
    catch
    end
end
val=sort(val)
[ok,msg]=checkValuesSquare2(val);
if ~ok disp(msg),return; end
disp('..........................7 ......OK');
clear val;

disp('..........................8 PAwaitFor');
resl = PAsolve(@funcSquare,{1},{2},{3},{'a'},{5});
j=1;
for i=1:5
    try
        val(j)=PAwaitFor(resl(i),timeout)
         j=j+1;
    catch
    end
end
[ok,msg]=checkValuesSquare2(val);
if ~ok disp(msg),return; end
disp('..........................8 ......OK');
clear val;

disp('..........................9 val and logs attributes');
resl = PAsolve(@funcSquare,{1},{2},{3},{'a'},{5});
j=1;
for i=1:5
    try
        val{j}=resl(i).val;
        logs{j}=resl(i).logs;
    catch
    end
    j=j+1;
end
[ok,msg]=checkValuesSquare(val);
if ~ok disp(msg),return; end
disp('..........................9 ......OK');
clear val;
clear logs;

function [ok,msg]=checkValuesFact(val)
[ok,msg]=checkValues(val,{1,2,6,24,120},'factorial');

function [ok,msg]=checkValuesSquare(val)
[ok,msg]=checkValues(val,{1,4,9,[],25}, 'funcSquare');

function [ok,msg]=checkValuesSquare2(val)
[ok,msg]=checkValues(val,{1,4,9,25}, 'funcSquare');

function [ok,msg]=checkValuesSquare3(val)
[ok,msg]=checkValues(val,{1,4,9}, 'funcSquare');

function [ok,msg]=checkValues(val,right,name)
if length(right) ~= length(val)
    ok = false;
    msg = 'Wrong number of outputs';
else
    for i=1:length(right)
        if iscell(val)
            if val{i} ~= right{i}
                ok = false;
                msg = ['TestBasic::Wrong value of ' name '(' num2str(i) '), received ' num2str(val{i}) ', expected ' num2str(right{i})];
            else
                ok = true;
                msg = [];
            end
        else
            if val(i) ~= right{i}
                ok = false;
                msg = ['TestBasic::Wrong value of ' name '(' num2str(i) '), received ' num2str(val(i)) ', expected ' num2str(right{i})];
            else 
                ok = true;
                msg = [];
            end
        end
    end

end



