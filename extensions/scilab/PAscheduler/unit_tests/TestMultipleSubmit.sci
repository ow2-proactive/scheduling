function [ok, msg]=TestMultipleSubmit(timeout)
    if ~exists('timeout')
        if (getos() == "Windows")
            timeout = 500000;
        else
            timeout = 200000;
        end
    end

    function [ok,msg]=checkValuesFact(val)
        [ok,msg]=checkValues(val,list(1,2,6,24,120),'factorial');
    endfunction

    function [ok,msg]=checkValues(val,right,name)
        if length(right) ~= length(val)
            ok = %f;
            msg = 'Wrong number of outputs';
        else
            for i=1:length(right)
                if typeof(val) == 'list'
                    if val(i) ~= right(i)
                        ok = %f;
                        msg = 'TestBasic::Wrong value of '+ name+ '(' + string(i) +'), received '+ string(val(i))+ ', expected ' + string(right(i));                
                    else
                        ok = %t;
                        msg = [];
                    end
                else
                    if val(i) ~= right(i)
                        ok = %f;
                        msg = 'TestBasic::Wrong value of '+ name +'(' + string(i)+ '), received '+ string(val(i))+ ', expected ' + string(right(i));

                    else 
                        ok = %t;
                        msg = [];
                    end
                end
            end
            if ~ok then
                disp('Received:');
                disp(val)
            end

        end
    endfunction

    disp('...... Testing PAsolve with multiple submits');

    disp('..........................submit 1');
    resl1 = PAsolve('factorial',1,2,3,4,5);
    disp('..........................submit 2');
    resl2 = PAsolve('factorial',1,2,3,4,5);   
    disp('..........................submit 3');
    resl3 = PAsolve('factorial',1,2,3,4,5); 
    disp('..........................submit 4');
    resl4 = PAsolve('factorial',1,2,3,4,5);  
    disp('..........................submit 5');
    resl5 = PAsolve('factorial',1,2,3,4,5);  

    val1=PAwaitFor(resl1,timeout)
    val2=PAwaitFor(resl2,timeout)
    val3=PAwaitFor(resl3,timeout)
    val4=PAwaitFor(resl4,timeout)
    val5=PAwaitFor(resl5,timeout)
    disp(val1);
    disp(val2);
    disp(val3);
    disp(val4);
    disp(val5);
    PAclearResults(resl1);
    PAclearResults(resl2);
    PAclearResults(resl3);
    PAclearResults(resl4);
    PAclearResults(resl5);
    [ok,msg]=checkValuesFact(val1);
    if ~ok error(msg),return; end
    [ok,msg]=checkValuesFact(val2);
    if ~ok error(msg),return; end
    [ok,msg]=checkValuesFact(val3);
    if ~ok error(msg),return; end
    [ok,msg]=checkValuesFact(val4);
    if ~ok error(msg),return; end
    [ok,msg]=checkValuesFact(val5);
    if ~ok error(msg),return; end
    if ok then
        disp('................................OK');
    end
endfunction


