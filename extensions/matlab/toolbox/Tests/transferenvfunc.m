function ok=transferenvfunc(in)
    mytransferenvvar = evalin('caller', 'mytransferenvvar')
    try
        disp(['Hello ' in ' and ' mytransferenvvar]);
        ok = true;
    catch
        disp(getReport(ME));
        ok = false;
    end
    ok = true;
end