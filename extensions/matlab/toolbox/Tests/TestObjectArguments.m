function [ok, msg]=TestObjectArguments(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 200000;
    else
        timeout = 80000;
    end
end
disp('...... Testing PAsolve with Object arguments');
 disp('..........................1 PAwaitAll');
d=dummy(55);
resl = PAsolve(@dummyfunc,{d});
val=PAwaitAll(resl,timeout)

if val.field2 == 22
    disp('..........................1 ......OK');
    ok=true;
    msg = [];
else
    disp('..........................1 ......KO');
    ok=false;
    msg = 'TestObjectArguments::wrong value for val.field2';
end