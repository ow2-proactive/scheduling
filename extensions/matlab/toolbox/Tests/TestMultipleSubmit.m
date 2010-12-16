function [ok, msg]=TestMultipleSubmit(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 500000;
    else
        timeout = 200000;
    end
end

disp('...... Testing PAsolve with multiple submits');

disp('..........................submit 1');
resl1 = PAsolve(@factorial,{1},{2},{3},{4},{5});
disp('..........................submit 2');
resl2 = PAsolve(@factorial,{1},{2},{3},{4},{5});   
disp('..........................submit 3');
resl3 = PAsolve(@factorial,{1},{2},{3},{4},{5}); 
disp('..........................submit 4');
resl4 = PAsolve(@factorial,{1},{2},{3},{4},{5});  
disp('..........................submit 5');
resl5 = PAsolve(@factorial,{1},{2},{3},{4},{5});  

val1=PAwaitAll(resl1,timeout)
val2=PAwaitAll(resl2,timeout)
val3=PAwaitAll(resl3,timeout)
val4=PAwaitAll(resl4,timeout)
val5=PAwaitAll(resl5,timeout)

[ok,msg]=checkValuesFact(val1);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val2);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val3);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val4);
if ~ok disp(msg),return; end
[ok,msg]=checkValuesFact(val5);
if ~ok disp(msg),return; end

function [ok,msg]=checkValuesFact(val)
[ok,msg]=checkValues(val,{1,2,6,24,120},'factorial');

function [ok,msg]=checkValues(val,right,name)
if length(right) ~= length(val)
    ok = false;
    msg = 'Wrong number of outputs';
else
    for i=1:length(right)
        if iscell(val)
            if val{i} ~= right{i}
                ok = false;
                msg = ['TestMultipleSubmit::Wrong value of ' name '(' num2str(i) '), received ' num2str(val{i}) ', expected ' num2str(right{i})];
            else
                ok = true;
                msg = [];
            end
        else
            if val(i) ~= right{i}
                ok = false;
                msg = ['TestMultipleSubmit::Wrong value of ' name '(' num2str(i) '), received ' num2str(val(i)) ', expected ' num2str(right{i})];
            else 
                ok = true;
                msg = [];
            end
        end
    end

end