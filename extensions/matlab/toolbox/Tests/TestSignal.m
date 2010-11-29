function [ok,msg]=TestSignal(timeout)

if ~exist('timeout', 'var')
    if ispc()
        timeout = 600000;
    else
        timeout = 300000;
    end
end

disp('...... Testing PAsolve with signal toolbox');
resl = PAsolve(@signalfunc,{1},{1},{1},{1},{1},{1});
val=PAwaitAll(resl,timeout)
for j=1:length(val)
    if val{j} ~= 1
        ok=false;
        msg='TestBigArrayAndKeepEngine::Some tasks didn''t succeed';
        return;
    end
end
ok=true;
msg='';