function A = PAisAwaited(this)
s=size(this);
for i=1:s(1)
    for j=1:s(2)
        R=this(i,j);
        f = R.future;
        A(i,j) = org.objectweb.proactive.api.PAFuture.isAwaited(f);
    end
end
