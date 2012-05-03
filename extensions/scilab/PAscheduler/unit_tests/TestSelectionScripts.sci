function [ok, msg]=TestSelectionScripts(timeout)
    if ~exists('timeout')
        if (getos() == "Windows")
            timeout = 200000;
        else
            timeout = 80000;
        end
    end
    disp('...... Testing PAsolve with Task Selection Script');
    opt = PAoptions();
    // Testing task selection script
    tsk = PATask(1,2);
    tsk(1,1:2).Func = 'cosh';
    tsk(1,1).SelectionScript = fullfile(opt.SchedulingDir, 'extensions', 'scilab', 'PAscheduler','unit_tests','script','truescript.js');
    tsk(1,1).Static = %t;
    tsk(1,2).SelectionScript = fullfile(opt.SchedulingDir,'extensions', 'scilab', 'PAscheduler', 'unit_tests', 'script','scriptwithparam.js');
    x = getdate("s");
    xx = getdate(x);
    sd='';for i=1:length(xx), sd=sd+string(xx(i))+'_',end    
    tsk(1,2).ScriptParams = sd;
    tsk(1,2).Static = %f;
    tsk(1,1).Params = list(1);
    tsk(1,2).Params = list(2);
    
    res = PAsolve(tsk);
    
    val = PAwaitFor(res, timeout);
    disp('..........................1 ......OK');
    
    // Adding global selection script
    disp('...... Testing PAsolve with Global Selection Script');
    oldsel = opt.CustomScript;
    oldstat = opt.CustomScriptStatic;
    oldparams = opt.CustomScriptParams;
    
    tsk(1,1).SelectionScript = [];
    tsk(1,1).Static = %f;
    tsk(1,2).SelectionScript = [];
        
    
    PAoptions('CustomScript',fullfile(opt.SchedulingDir,'extensions', 'scilab', 'PAscheduler', 'unit_tests', 'script','scriptwithparam.js'));
    PAoptions('CustomScriptStatic', %f);    
    x = getdate("s");
    xx = getdate(x);
    sd='';for i=1:length(xx), sd=sd+string(xx(i))+'_',end   
    PAoptions('CustomScriptParams', sd);
    
    res = PAsolve(tsk);
    
    val = PAwaitFor(res, timeout);
    
    disp('..........................2 ......OK');
    
    PAoptions('CustomScript', oldsel);
    PAoptions('CustomScriptStatic',oldstat);
    PAoptions('CustomScriptParams', oldparams);
    
    ok = %t;
    
    msg = [];
        
endfunction