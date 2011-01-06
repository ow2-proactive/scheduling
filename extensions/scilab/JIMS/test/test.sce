A=class("test");
 
for i=1:10,for j=1:10,a=rand(i,j);if unwrap(A.F(a)) <> a then,disp("bug");end;end;end
 
for i=1:10,for j=1:10,a=int8(10*rand(i,j));if unwrap(A.F(a)) <> a then,disp("bug");end;end;end
 
for i=1:10,for j=1:10,a=int16(10*rand(i,j));if unwrap(A.F(a)) <> a then,disp("bug");end;end;end
 
for i=1:10,for j=1:10,a=int32(10*rand(i,j));if unwrap(A.F(a)) <> a then,disp("bug");end;end;end
 
for i=1:10,for j=1:10,a=uint8(10*rand(i,j));if unwrap(A.F(a)) <> a then,disp("bug");end;end;end
 
for i=1:10,for j=1:10,a=uint16(10*rand(i,j));if unwrap(A.F(a)) <> a then,disp("bug");end;end;end
 
for i=1:10,for j=1:10,a=uint32(10*rand(i,j));if unwrap(A.F(a)) <> a then,disp("bug");end;end;end
 
for i=1:10,for j=1:10,a=string(rand(i,j));if unwrap(A.F(a)) <> a then,disp("bug");end;end;end   

for i=1:10,for j=1:10,b=rand(i,j);a=wrapinfloat(b);if abs(unwrap(A.F(a))-b) > 1E-6 then,disp("bug");end;end;end

for i=1:10,for j=1:10,b=uint16(1024*rand(i,j));a=wrapinchar(b);if unwrap(A.F(a)) <> b then,disp("bug");end;end;end

for i=1:10,for j=1:10,a=(floor(2*rand(i,j))==1);if unwrap(A.F(a)) <> a then,disp("bug");end;end;end