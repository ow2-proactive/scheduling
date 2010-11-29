function A = PAwaitAny(this,timeout)
s=size(this);
arrayList = java.util.ArrayList();
indList={};
k=1;
for i=1:s(1)
    for j=1:s(2)
        R=this(i,j);
        if ~R.waited.get()
            arrayList.add(R.future);
            indList{k}=[i j];
            k=k+1;
        end
    end
end

if isempty(indList)
    error('All results have already been accessed');
end

if exist('timeout','var') == 1
    ind = org.objectweb.proactive.api.PAFuture.waitForAny(arrayList,int64(timeout));
else
    ind = org.objectweb.proactive.api.PAFuture.waitForAny(arrayList);
end
l=indList{ind+1};
R=this(l(1),l(2));
R.waited.set(true);
A = PAwaitFor(R);


