function [ok, msg]=TestTransferEnv(timeout)
    if ~exists('timeout')
        if (getos() == "Windows")
            timeout = 500000;
        else
            timeout = 200000;
        end
    end
    disp('...... Testing PAsolve with TransferEnv');
    opt=PAoptions();
    oldTransferEnv = opt.TransferEnv;
    PAoptions('TransferEnv',%t);
    toto = 'Hello Toto';
    global ('totoGlobal');
    totoGlobal = 'Hello Toto Global';

    function [out]=myHello(in)
        global('totoGlobal')      
        disp(toto);
        disp(totoGlobal);    
        out=%t;
    endfunction


    r=PAsolve('myHello',%t);
    val=PAwaitFor(r,timeout);
    ok = val(1);
    PAclearResults(r);

    if ok then
        disp('................................OK');
    end
    msg=[];
    PAoptions('TransferEnv',oldTransferEnv);
endfunction

