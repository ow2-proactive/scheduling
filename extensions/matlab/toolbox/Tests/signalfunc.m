function ok=signalfunc(in)
[b,a] = cheby1(12,0.5,200/500);
[h,f] = freqz(b,a,256,1000);
ok=1;