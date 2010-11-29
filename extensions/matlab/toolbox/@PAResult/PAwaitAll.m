function varargout = PAwaitAll(this,timeout)
s=size(this);
arrayList = java.util.ArrayList();
for i=1:s(1)
    for j=1:s(2)        
      R=this(i,j);      
      arrayList.add(R.future);      
    end
end
if exist('timeout','var') == 1
    org.objectweb.proactive.api.PAFuture.waitForAll(arrayList,int64(timeout));
else
    org.objectweb.proactive.api.PAFuture.waitForAll(arrayList);
end
for i=1:s(1)
    for j=1:s(2) 
        R=this(i,j);
        if nargout > 1 || (s(1) == 1 && s(2) == 1)
            try
                varargout{(i-1)*s(2)+j} = PAwaitFor(R);            
            catch ME
                ME.stack;
                varargout{(i-1)*s(2)+j} = [];
            end
        else
            try
                varargout{1}{(i-1)*s(2)+j} = PAwaitFor(R); 
             catch ME
                ME.stack;
                varargout{1}{(i-1)*s(2)+j} = [];
            end   
        end
    end
end
