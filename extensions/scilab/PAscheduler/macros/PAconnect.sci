function [] = PAconnect(uri)

    global ('PA_initialized', 'PA_connected')

    if ~exists('PA_initialized') | PA_initialized ~= 1
        PAinit();
    end
    if ~exists('PA_connected') | PA_connected ~= 1
        connect(uri,"","");
        PA_connected = 1;
        disp(strcat(['Connection successful to ', uri]));
    else
        disp('Already connected');
    end

endfunction