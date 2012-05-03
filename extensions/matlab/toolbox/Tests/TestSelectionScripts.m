function [ok, msg]=TestSelectionScripts(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 500000;
    else
        timeout = 200000;
    end
end
opt = PAoptions();
% Testing task selection script
tsk = PATask(1,2);
tsk(1,1:2).Func = @factorial;
tsk(1,1).SelectionScript = fullfile(opt.SchedulingDir, 'extensions', 'matlab', 'toolbox','Tests','script','truescript.js');
tsk(1,1).Static = true;
tsk(1,2).SelectionScript = fullfile(opt.SchedulingDir,'extensions', 'matlab', 'toolbox', 'Tests', 'script','scriptwithparam.js');
xx = clock();
sd='';
for i=1:length(xx)
    sd=[sd num2str(xx(i)) '_'];
end
tsk(1,2).ScriptParams = sd;
tsk(1,2).Static = true;
tsk(1,1).Params = 1;
tsk(1,2).Params = 2;

res = PAsolve(tsk);

val = PAwaitFor(res, timeout);

% Adding global selection script

oldsel = opt.CustomScript;
oldstat = opt.CustomScriptStatic;
oldparams = opt.CustomScriptParams;

tsk(1,1).SelectionScript = [];
tsk(1,1).Static = false;
tsk(1,2).SelectionScript = [];


PAoptions('CustomScript',fullfile(opt.SchedulingDir,'extensions', 'matlab', 'toolbox', 'Tests', 'script','scriptwithparam.js'));
PAoptions('CustomScriptStatic', false);
xx = clock();
sd='';
for i=1:length(xx)
    sd=[sd num2str(xx(i)) '_'];
end
PAoptions('CustomScriptParams', sd);

res = PAsolve(tsk);

val = PAwaitFor(res, timeout);

PAoptions('CustomScript', oldsel);
PAoptions('CustomScriptStatic',oldstat);
PAoptions('CustomScriptParams', oldparams);

ok = true;

msg = [];