%timerFcn
function timerFcn(obj, event, end_time, param_files, han , hanTXTPerc, curr_dir, Go_plot_button, pushbutton_start, Sim_Matlab_button, simulation_status, LEDhandles, stopTimer_Callback, time, resList,jobid)
try
    files = param_files;
    finals = zeros(1,length(files));

    %load the info regarign the Sample time in the mat-files
    if (exist([curr_dir filesep 'tol.mat']) == 2)
        load([curr_dir filesep 'tol.mat'],'tolerance');
    else
        tolerance = 0;
    end
    for i=1:length(files)
        file = strtrim(files{i});

        dir = [curr_dir filesep file(1:end-2)];


        %to be changed to what(dir)
        if (exist(dir) == 7)
            files_in_dir = what(dir);
            if length(files_in_dir.mat) > 0
                fid = fopen([dir filesep files_in_dir.mat{1}]);
                if (fid ~= -1)
                    try
                        %position the file to the number of rows
                        fseek(fid,4,-1);
                        num_rows = fread(fid,1,'int32');
                        fseek(fid,0,1);
                        endf = ftell(fid);
                        if (endf >= 32) && num_rows > 0
                            offset = mod(endf - 24,8*num_rows);
                            if offset == 0
                                offset = 8*num_rows;
                            end
                            fseek(fid, -offset, 1);
                            finals(i)=fread(fid,1,'double');
                        end
                    catch ME
                        disp(ME.stack(1))
                    end
                    fclose(fid);
                end
            end
        end
    end
    finalmin = mean(finals);
    if (finalmin +tolerance < end_time) && (finalmin >= 0)
        updateBar(han, hanTXTPerc, finalmin, end_time, LEDhandles);
    elseif finalmin > end_time - tolerance
        updateBar(han, hanTXTPerc, finalmin, end_time, LEDhandles);
        cd(curr_dir);
        endtime=clock;
        dur=etime(endtime,time);
        try
            set(hanTXTPerc,'String',['Finished ' num2str(dur) ' sec'],'Visible','on');
        catch 
        end
            
        stopTimer_Callback(obj, curr_dir, Go_plot_button,pushbutton_start, Sim_Matlab_button,simulation_status, resList,jobid);
    end
catch ME
    disp(getReport(ME));
end



function updateBar(han, hanTXTPerc, finalmin, end_time, LEDhandles)
%modify code here
%color the block if percentage passes
LED=10;  %You can use more LED lights. Create them in 'guide' for main GUI

try
    set(han,'Visible','off');
    set(hanTXTPerc,'Visible','on');
catch 
end
pause(0.000001)  %need this so their showing up will not have a delay
%LED progress bar preparation finished
%set LED progress bar below
percentDone_string=[num2str(round(finalmin/end_time*100)),'% Done'];
if (finalmin~=-1)
    try
        set(hanTXTPerc,'String',percentDone_string,'Visible','on'); %LED %done
    catch 
    end
end
%if i/imax >= progressmark*0.99   %Use a smaller number (e.g., 0.8) to
%show LED earlier
for i = 1:ceil(10*(finalmin-eps)/end_time)
    try
        set(LEDhandles{i},'Visible','on');
    catch 
    end   
end

pause(0.000001)  %need this so that LED lighting will not fall behind %done!
%    progressmark=progressmark+stepsize;
%end
%LED progress bar block ends

%waitbar(finalmin/str2num(end_time),h);


