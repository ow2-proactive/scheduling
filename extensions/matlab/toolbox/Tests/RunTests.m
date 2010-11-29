function [ok, msg]=RunTests(nbiter,timeout)

runimage = true;
runsignal = true;

for i=1:nbiter
    disp(['Iteration ' num2str(i)]);
    if exist('timeout', 'var')
        [ok,msg] = TestBasic(timeout);
    else
        [ok,msg] = TestBasic();
    end    
    if ~ok disp(msg),return; end
    if exist('timeout', 'var')
        [ok,msg] = TestCompose(timeout);
    else
        [ok,msg] = TestCompose();
    end
    if ~ok disp(msg),return; end
    if exist('timeout', 'var')
        [ok,msg] = TestObjectArguments(timeout);
    else
        [ok,msg] = TestObjectArguments();
    end
    if ~ok disp(msg),return; end
    if exist('timeout', 'var')
        [ok,msg] = TestBigArrayAndKeepEngine(timeout);
    else
        [ok,msg] = TestBigArrayAndKeepEngine();
    end
    if ~ok disp(msg),return; end
    if runimage
        if exist('timeout', 'var')
            [ok,msg] = TestPATask(timeout);
        else
            [ok,msg] = TestPATask();
        end
    end
    if ~ok disp(msg),return; end
    if runsignal
        if exist('timeout', 'var')
            [ok,msg] = TestSignal(timeout);
        else
            [ok,msg] = TestSignal();
        end
    end
    if ~ok disp(msg),return; end
    if exist('timeout', 'var')
        [ok,msg] = TestMultipleSubmit(timeout);
    else
        [ok,msg] = TestMultipleSubmit();
    end
    if ~ok disp(msg),return; end
end
