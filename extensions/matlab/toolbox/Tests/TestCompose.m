function [ok, msg]=TestCompose(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 500000;
    else
        timeout = 200000;
    end
end
format long
t(1:5) = PATask;
t(1:5).Func = @mysqrt;
t(1).Params = 1;
t(2).Params = 2;
t(3).Params = 3;
t(4).Params = 4;
t(5).Params = 5;
t(2,1:5) = t(1:5);
t(2,1:5).Params = {};
t(2,1:5).Compose = true;
t(3,1:5) = t(1:5);
t(3,1:5).Params = {};
t(3,1:5).Compose = true;
t

 disp('...... Testing PAsolve with sqrt(sqrt(sqrt(x)))');
 disp('..........................1 PAwaitAll');
 resl = PAsolve(t);
 val=PAwaitAll(resl,timeout)
 [ok,msg]=checkValues(val);
if ~ok disp(msg),return; end
 disp('..........................1 ......OK');
 clear val;
 
 disp('..........................2 PAwaitAny');
resl = PAsolve(t);
for i=1:5
    val(i)=PAwaitAny(resl,timeout)
end
val=sort(val)
[ok,msg]=checkValues(val);
if ~ok disp(msg),return; end
disp('..........................2 ......OK');
clear val;

function [ok,msg]=checkValues(val)
right=num2cell(sqrt(sqrt(sqrt(1:5))))
if length(right) ~= length(val)
    ok = false;
    msg = 'Wrong number of outputs';
else
    for i=1:length(right)
        if iscell(val)
            if val{i} ~= right{i}
                ok = false;
                msg = ['TestCompose::Wrong value of sqrt(sqrt(sqrt(' num2str(i) '))), received ' num2str(val{i}) ', expected ' num2str(right{i})];
            else
                ok = true;
                msg = [];
            end
        else
            if val(i) ~= right{i}
                ok = false;
                msg = ['TestCompose::Wrong value of sqrt(sqrt(sqrt(' num2str(i) '))), received ' num2str(val(i)) ', expected ' num2str(right{i})];
            else 
                ok = true;
                msg = [];
            end
        end
    end

end



