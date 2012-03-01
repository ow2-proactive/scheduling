function [ok, msg]=TestTopology(timeout)
    if ~exists('timeout')
        if (getos() == "Windows")
            timeout = 500000;
        else
            timeout = 200000;
        end
    end
    disp('...... Testing PAsolve with topology');
    t = PATask(1,4);
    t(1,1:4).Func = 'myHello';
    t(1,1:4).NbNodes = 2;
    t(1,1:4).Topology = 'bestProximity';

    t(1,1).Params = list('Dude1');
    t(1,2).Params = list('Dude2');
    t(1,3).Params = list('Dude3');
    t(1,4).Params = list('Dude4');
    t

    function [out]=myHello(in)
        nl = NODE_LIST; 
        nul = NODE_URL_LIST;   
        disp('Number of nodes used : ' + string(length(nl)));
        for i=1:length(nl)
            disp('Node n°' + string(i) + ': ' + nl(i) + ' ' + nul(i));
        end
        disp('Hello '+in);
        printf('\n');
        out=%t;
    endfunction

    r=PAsolve(t);
    val=PAwaitFor(r,timeout);
    PAclearResults(r);
    ok = %t;
    for e=val
        ok = ok & e;
    end
    if ok then
        disp('................................OK');
    end
    msg=[];
endfunction


