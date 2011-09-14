function c=%PATask_c_PATask(a,b)
    c = mlist(['PATask','matrix']);
    c.matrix = [a.matrix, b.matrix];
endfunction