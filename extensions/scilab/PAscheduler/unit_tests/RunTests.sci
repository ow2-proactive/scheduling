function [ok, msg]=RunTests(nbiter,timeout)

for i=1:nbiter
    disp(['Iteration ' string(i)]);
    if exists('timeout')
        [ok,msg] = TestBasic(timeout);
    else
        [ok,msg] = TestBasic();
    end
    if ~ok disp(msg),return; end
    
    if exists('timeout')
        [ok,msg] = TestCompose(timeout);
    else
        [ok,msg] = TestCompose();
    end
    if ~ok disp(msg),return; end
    
    if exists('timeout')
        [ok,msg] = TestPATask(timeout);
    else
        [ok,msg] = TestPATask();
    end
    if ~ok disp(msg),return; end
    
    if exists('timeout')
        [ok,msg] = TestMultipleSubmit(timeout);
    else
        [ok,msg] = TestMultipleSubmit();
    end
    if ~ok disp(msg),return; end
    
    if exists('timeout')
        [ok,msg] = TestTopology(timeout);
    else
        [ok,msg] = TestTopology();
    end
    if ~ok disp(msg),return; end
    jremove();
end


endfunction