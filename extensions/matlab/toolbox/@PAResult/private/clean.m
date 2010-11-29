function clean(R)
if ~R.cleaned.get()
    warning('off')
    setf = R.cleanFileSet;
    for i=1:length(setf)
        delete(setf{i});
    end   
    setd = R.cleanDirSet;
    for i=1:length(setd)
        d=dir(setd{i});
        if length(d) == 2
            rmdir(setd{i});
        end
    end    
    R.cleaned.set(1);
    warning('on')
end
