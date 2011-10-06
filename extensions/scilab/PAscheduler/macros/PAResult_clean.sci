function PAResult_clean(R)
    if ~jexists(R.cleaned) then
        return;
    end
    if ~jinvoke(R.cleaned,'get')
        warning('off')
        setf = R.cleanFileSet;
        for i=1:length(setf)
            mdelete(setf(i));
        end
        setd = R.cleanDirSet;
        for i=1:length(setd)
            d=ls(setd(i));
            if (getos() == "Linux") & size(d,1) == 2
                rmdir(setd(i));
            elseif (getos() == "Windows") & size(d,1) == 0
                rmdir(setd(i));
            end
        end
        //sched = PAScheduler;
        //sched.PATaskRepository(R.jobid, R.taskid, 'received');
        jinvoke(R.cleaned,'set',%t);
        warning('on')
    end
endfunction