function [ok, msg]=TestTopology(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 200000;
    else
        timeout = 80000;
    end
end
disp('...... Testing PAsolve with topology');
t = PATask(1,4);
t(1,1:4).Func = @myHello;
t(1,1:4).NbNodes = 2;
t(1,1:4).Topology = 'bestProximity';

t(1,1).Params = 'Dude1';
t(1,2).Params = 'Dude2';
t(1,3).Params = 'Dude3';
t(1,4).Params = 'Dude4';
t

r=PAsolve(t);
val=PAwaitFor(r,timeout);
ok=all([val{:}]);
msg=[];
end
