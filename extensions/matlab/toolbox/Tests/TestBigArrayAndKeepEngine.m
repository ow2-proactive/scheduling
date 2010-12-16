function [ok, msg]=TestBigArrayAndKeepEngine(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 1600000;
    else
        timeout =800000;
    end
end
opt = PAoptions;
oldval = opt.KeepEngine;
PAoptions('KeepEngine',true);
disp('.................................. Testing PAsolve with big array and Keep Engine');
disp('............................First create a out of memory error on distant engines');
resl = PAsolve(@makeBigArray,{100000},{100000},{100000},{100000});
try 
val=PAwaitAll(resl,timeout)
catch err
end

disp('....Then create more big matrixes on the same engines (we verify that they recover)');
for i=1:8
    disp(['..........................Iteration ' num2str(i)]);
    resl = PAsolve(@makeBigArray,{i*1000},{i*1000},{i*1000},{i*1000});
    val=PAwaitAll(resl,timeout)
    for j=1:length(val)
        if val{j} ~= 1
            ok=false;
            msg='TestBigArrayAndKeepEngine::Some tasks didn''t succeed';
            return;
        end        
    end
    disp(['...........................' num2str(i) ' ......OK']);
    PAoptions('KeepEngine',oldval);
    ok=true;
    msg=[];
end
