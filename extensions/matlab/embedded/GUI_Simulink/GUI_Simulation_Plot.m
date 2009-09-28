 function varargout = GUI_Simulation_Plot(varargin)
% GUI_SIMULATION_PLOT M-file for GUI_Simulation_Plot.fig
%      GUI_SIMULATION_PLOT, by itself, creates a new GUI_SIMULATION_PLOT or raises the existing
%      singleton*.
%
%      H = GUI_SIMULATION_PLOT returns the handle to a new GUI_SIMULATION_PLOT or the handle to
%      the existing singleton*.
%
%      GUI_SIMULATION_PLOT('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in GUI_SIMULATION_PLOT.M with the given input arguments.
%
%      GUI_SIMULATION_PLOT('Property','Value',...) creates a new GUI_SIMULATION_PLOT or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before GUI_Simulation_Plot_OpeningFunction gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to GUI_Simulation_Plot_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help GUI_Simulation_Plot

% Last Modified by GUIDE v2.5 31-Aug-2009 19:00:28

% Begin initialization code - DO NOT EDIT

%for THALES uncomment lines 62-64, 934-937, 134. Comment 132.

gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @GUI_Simulation_Plot_OpeningFcn, ...
                   'gui_OutputFcn',  @GUI_Simulation_Plot_OutputFcn, ...
                   'gui_LayoutFcn',  [] , ...
                   'gui_Callback',   []);
if nargin && ischar(varargin{1})
    gui_State.gui_Callback = str2func(varargin{1});
end

if nargout
    [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
else
    gui_mainfcn(gui_State, varargin{:});
end
% End initialization code - DO NOT EDIT


% --- Executes just before GUI_Simulation_Plot is made visible.
function GUI_Simulation_Plot_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to GUI_Simulation_Plot (see VARARGIN)

% Choose default command line output for GUI_Simulation_Plot
handles.output = hObject;
%[PATHSTR,NAME,EXT,VERSN] = fileparts(mfilename('fullpath'));
curr = pwd;

ht = java.util.Properties;
set(handles.tree,'UserData',ht);
addpath(curr);
set(handles.curr_dir,'UserData',pwd);
set(handles.Go_plot_button,'Enable','off');
set(handles.simulation_status,'Enable','off');
set(handles.clear_plots_button,'Enable','off');
set(handles.current_directory,'String',curr);
global hy;
hy = java.util.Properties; 

%initializing the param files
set(handles.listbox_param_files,'String',{});
set(handles.listbox_param_files,'Value',0);

%initializing the mat files
set(handles.listbox_mat_files,'String',{});
set(handles.listbox_mat_files,'Value',0);
set(handles.listbox_mat_files,'Enable','off');
set(handles.add_mat_button,'Enable','off');
set(handles.remove_mat_button,'Enable','off');
set(handles.pushbutton_remove,'Enable','off');

%if no selection change happens, use *.mdl as default
ud.filter = 'mdl';
set(handles.edit_model,'UserData',ud);  

%show up a selection msgbox regardless of the existence of the M_data file
choose_button_mod = questdlg('Would you like to load an existing model ?','Load Model');
if strcmp(choose_button_mod, 'Yes')
    %set the correct path where the model is
    %show a UI
    [M_data_name, M_data_dir] = uigetfile('*.mat','Select the MAT-file with the model info','M_data.mat');
    %perform the following actions if no cancel is pressed
    if (~isnumeric(M_data_name) && strcmp(M_data_name,'M_data.mat'))    
        %perform a CD to the selected directory, add it to the path and update
        %the current directort bar
        cd(M_data_dir);
        addpath(M_data_dir);
        set(handles.current_directory,'String',M_data_dir);
        load M_data;
        set(handles.current_directory, 'String', M_data.curr_dir);
        set(handles.edit_model,'String', M_data.model_Name);
        set(handles.listbox_param_files,'Value',1);
        set(handles.listbox_param_files,'String', M_data.param_Files);
        set(handles.Simulation_end_time,'String',M_data.end_time);
        if ~isempty(M_data.param_Files)
            set(handles.pushbutton_remove,'Enable','on');
            set(handles.Sim_Matlab_button,'Enable','on');
            set(handles.Sim_Matlab_button,'Enable','on');
            %set(handles.resume_simulation,'Visible','on');
            set(handles.pushbutton_start,'Enable','off');
            set(handles.listbox_mat_files,'Enable','on');
            continue_simulation(hObject, eventdata, handles);
        end
    end    
end

% determine if connection to the scheduler was established

%

solver = PAgetsolver();
if strcmp(class(solver),'double')
    error('A connection to ProActive Scheduler must be established before using the GUI');
end

%check if the M_data mat-file exists 

 

% if exist('M_data.mat') == 2
%     choose_button = questdlg('An existing model was detected. Would you like to load it ?','Model detected');
%     if strcmp(choose_button, 'Yes')
%         load M_data;
%         set(handles.current_directory, 'String', M_data.curr_dir);
%         set(handles.edit_model,'String', M_data.model_Name);
%         set(handles.listbox_param_files,'Value',1);
%         set(handles.listbox_param_files,'String', M_data.param_Files);
%         set(handles.Simulation_end_time,'String',M_data.end_time);
%         if ~isempty(M_data.param_Files)
%             set(handles.pushbutton_remove,'Enable','on');
%             set(handles.Sim_Matlab_button,'Enable','on');
%             set(handles.Sim_Matlab_button,'Enable','on');
%             %set(handles.resume_simulation,'Visible','on');
%             set(handles.pushbutton_start,'Enable','off');
%             set(handles.listbox_mat_files,'Enable','on');
%             continue_simulation(hObject, eventdata, handles);
%         end   
%     else
%         load M_data;
%         delete([M_data.curr_dir filesep 'M_data.mat']);
%     end
%     
% end

fig_data = imread('Logo-ProActive_small.png');

axes(handles.axes2);
set(handles.axes2,'Visible','off');
imagesc(fig_data)
axis off
%switch off progress bars
LED = 10;
for n=1:LED
    LEDhandles(n)=findobj('Tag',['textLED' num2str(n)]); 
    set(LEDhandles(n),'Visible','off');
end
guidata(hObject, handles);

% UIWAIT makes GUI_Simulation_Plot wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = GUI_Simulation_Plot_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes on button press in pushbutton_select.
function pushbutton_select_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_select (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
cd(get(handles.curr_dir,'UserData'));
ud = get(handles.edit_model,'UserData');
[str_File, str_Path] = uigetfile('*.mdl','Select Simulation Model');
if ~isnumeric(str_File) 
    set(handles.edit_model,'String',str_File);
    set(handles.edit_model,'UserData',str_Path);
    cd(str_Path);
end
%upon selecting the Model, perform a cd to the directory where the model is

% whenever cd is called, modify the string containing the current directory


%set(handles,'UserData', )



function edit_model_Callback(hObject, eventdata, handles)
% hObject    handle to edit_model (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit_model as text
%        str2double(get(hObject,'String')) returns contents of edit_model as a double


% --- Executes during object creation, after setting all properties.
function edit_model_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit_model (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes on selection change in listbox_param_files.
function listbox_param_files_Callback(hObject, eventdata, handles)
% hObject    handle to listbox_param_files (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = get(hObject,'String') returns listbox_param_files contents as cell array
%        contents{get(hObject,'Value')} returns selected item from listbox_param_files
%cd(get(handles.curr_dir,'UserData'));
global hy;
index_selected = get(handles.listbox_param_files,'Value');
if ~isempty(index_selected)
    list = get(handles.listbox_param_files,'String');
    if ~isempty(list)
        param_name=list{index_selected};
    
        %this should already exist
        if ~(exist(param_name(1:end-2)) == 7)
            mkdir(param_name(1:end-2));
        end
        %%cd(param_name(1:end-2));
        set(handles.listbox_mat_files,'Visible','on');
        set(handles.listbox_mat_files,'Value',1);
        set(handles.add_mat_button,'Enable','on');
        set(handles.text5,'String',param_name(1:end-2));
        
        %load the mat-files associated
        param_name(1:end-2);
        ht = get(handles.tree,'UserData');
        s_l = ht.get(param_name(1:end-2));
        shows = {};
        if ~isempty(s_l)
            shows = cell(s_l);
            set(handles.remove_mat_button,'Enable','on');
            set(handles.Go_plot_button,'Enable','on');
        else
            set(handles.Go_plot_button,'Enable','off');
            set(handles.remove_mat_button,'Enable','off');
        end
        % need to convert show_list from java.lang.String[]: to cell array 
        set(handles.listbox_mat_files,'String',shows);
        
    end
end




% --- Executes during object creation, after setting all properties.
function listbox_param_files_CreateFcn(hObject, eventdata, handles)
% hObject    handle to listbox_param_files (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: listbox controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes on button press in pushbutton_add.
function pushbutton_add_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_add (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
%%cd(get(handles.curr_dir,'UserData'));

%specify the directory to add from

str_File = uigetfile('*.m','Select Parameter File');

%delete the file if the directory already exists
%%if (exist(str_File(1:end-2)) == 7)
    %%cd(str_File(1:end-2));
%    delete *.mat;
%    cd ..
%end

if ~isnumeric(str_File)
    str_File = strtrim(str_File);
    ht = get(handles.tree,'UserData');


    str_List_old = get(handles.listbox_param_files,'String');
    %    str_List_cell = get(handles.listbox_param_files,'UserData');
    %    if ~iscell(str_List_cell)
    %        str_List_cell = {str_File};
    %    else
    %        str_List_cell = [ str_List_cell {str_File} ];
    %    end

    if ~ismember(str_File,str_List_old)
        str_List_new = [ str_List_old; {str_File} ];

        if isempty(str_List_old)
            %        str_List_new = {str_File};
            set(handles.listbox_param_files,'Value',1);
            %   else
            %       str_List_new = [ str_List_old {str_File} ];
        end

        set(handles.listbox_param_files,'String',str_List_new);
        %   set(handles.listbox_param_files,'UserData',str_List_cell);
        set(handles.pushbutton_remove,'Enable','on');
        listbox_param_files_Callback(hObject, eventdata, handles);
        set(handles.listbox_mat_files,'Enable','on');
        set(handles.add_mat_button,'Enable','on');
    end
end


% --- Executes on button press in pushbutton_start.
function pushbutton_start_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_start (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% set(handles.simulation_status,'String','the simulation is being run ');

modelName = get(handles.edit_model,'String');
if isempty(modelName)
    msgbox('Please choose a simulation model','','error'); 
    return; 
end
save_sampleTime_info(hObject, eventdata, handles);
param_files = get(handles.listbox_param_files,'String');
if isempty(param_files)
    msgbox('Please add a parameter file to the list','','error');
    return;
elseif length(param_files) > 1
    index_selected = get(handles.listbox_param_files,'Value');
    if isempty(index_selected)
       msgbox('Please select a parameter file from the list','','error'); 
       return;
    end
    param_file = param_files{index_selected};
else
    param_file = param_files{1};
end

param_file_dir = param_file(1:end-2);

end_time = str2num(get(handles.Simulation_end_time,'String'));

if (end_time <= 0) 
    msgbox('Please define a simulation end time greater than 0','','error');
    return;
end
if isempty(end_time)
    msgbox('Please define a simulation end time greater than 0','','error');
    return;
end
set(handles.simulation_status,'Visible','on');
set(handles.pushbutton_start,'Enable','off');
set(handles.Sim_Matlab_button,'Enable','off');


model_Name = get(handles.edit_model,'String');
PAeval_param = cell(1,length(param_files));
param_files = get(handles.listbox_param_files,'String');
for i = 1:length(param_files)
    str_app = strtrim(param_files{i});
    if (exist(str_app(1:end-2)) == 7)
        rmdir(str_app(1:end-2),'s');
    end
end
LED = 10;
for i = 1:LED
    LEDhandles(i)=findobj('Tag',['textLED' num2str(i)]);
    set(LEDhandles(i),'Visible','off');
end
set(handles.textLEDpercentDone,'Visible','off');
%set up the sampling period
period = 1;
end_time = str2num(get(handles.Simulation_end_time,'String'));
curr_dir = get(handles.curr_dir,'UserData');

Save_Mat_File_Info(hObject, eventdata, handles);
%create a timer object here

T = timer('TimerFcn', {'timerFcn',end_time, param_files, handles.editLEDbkground, handles.textLEDpercentDone, curr_dir, handles.Go_plot_button, handles.pushbutton_start, handles.Sim_Matlab_button, handles.simulation_status, {handles.textLED1, handles.textLED2, handles.textLED3, handles.textLED4, handles.textLED5, handles.textLED6, handles.textLED7, handles.textLED8, handles.textLED9, handles.textLED10}, @stopTimer_Callback} ,'ExecutionMode','FixedRate','Period',period);
%starting the timer before executing

%saving the mat-file

start(T);

resl = {};
for i = 1:length(param_files)

    str_cur_param = strtrim(param_files{i});
    str_cur_param = str_cur_param(1:end-2);
    if exist(str_cur_param) == 7
        rmdir(str_cur_param,'s');
    end
    mkdir(str_cur_param);
    newDir = str_cur_param;
    copyfile(param_files{i},[newDir filesep param_files{i}], 'f');
    copyfile(model_Name,[newDir filesep model_Name], 'f');
    %need to copy the timerFcn too 
    %copyfile('timerFcn.m',[newDir filesep 'timerFcn.m'],'f');
    set(handles.Go_plot_button,'Visible','on');
    %% creates the new file to specific foler assiciated to the param file
    %% the current directory is not meant to be pwd, bu the one in which
    %% the value of the path to the selected modelname is stored
    %curr_dir = pwd ;
    curr_dir = get(handles.edit_model,'UserData');
    PAeval_param{i}.modelName =  model_Name;
    PAeval_param{i}.paramFile = param_files{i};
    PAeval_param{i}.pwd_dir = [curr_dir filesep str_cur_param];
    PAeval_param{i}.end_time = str2num(get(handles.Simulation_end_time,'String'));
    PAeval_param{i}.current_dir = curr_dir;

    res = PAeval(@PAsolve_modelname,PAeval_param{i});
    resl = [resl {res}];
end


% for i = 1:length(param_files)
%     res = resl{i};
%     res.wait;
% end

%stop(T)

%set(handles.simulation_status,'String','the simulation ended ');
%set(handles.Go_plot_button,'Visible','on');
%set(handles.pushbutton_start,'Enable','on');
%set(handles.Sim_Matlab_button,'Enable','on');
%set(handles.simulation_status,'String','the simulations ended');
%set(handles.simulation_status,'Visible','on');



% --- Executes on button press in Go_plot_button.
function Go_plot_button_Callback(hObject, eventdata, handles)
% hObject    handle to Go_plot_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
param_list = get(handles.listbox_param_files,'String');
str_cur_param = get(handles.text5,'String');
% selected mat files
mat_files = get(handles.listbox_mat_files,'String');
%for i = 1:length(param_list)
    
    %s=what(str_cur_param);
    %mat_files = s.mat;
    for j = 1:length(mat_files)
        A{j} = load([str_cur_param filesep mat_files{j}]);
        P = A{j}.ans;
        
        % add special features to the figure
        figure('Name',num2str(j));
        plot(P(1,:),P(2:end,:))
        index_1 = findstr(str_cur_param,'_');
        index_2 = findstr(mat_files{j},'_');
        str_cur_param_mod = str_cur_param;
        str_cur_param_mod(index_1) = ' ';
        str_mat = mat_files{j};
        str_mat(index_2) = ' ';
        title([char(str_cur_param_mod) ' ' char(str_mat)   ]);
        grid
    end

%end
%set(handles.simulation_status,'Visible','off');
%set(handles.Go_plot_button,'Visible','off');
%cd (get(handles.curr_dir,'UserData'))
set(handles.clear_plots_button,'Enable','on');


% --- Executes on button press in pushbutton_exit.
function pushbutton_exit_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_exit (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
%cd (get(handles.curr_dir,'UserData'))
delete(gcf)



% --- Executes on button press in .
function pushbutton_remove_Callback(hObject, eventdata, handles)
% hObject    handle to pushbutton_remove (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
index_selected = get(handles.listbox_param_files,'Value');
%str_List_cell = get(handles.listbox_param_files,'UserData');
list = get(handles.listbox_param_files,'String');
remain = setdiff(1:size(list,1),[index_selected]);
list = list(remain);
%str_List_cell = str_List_cell(remain);
set(handles.listbox_param_files,'String',list);
%set(handles.listbox_param_files,'UserData',str_List_cell);
set(handles.listbox_param_files,'Value',1.0);
if length(list) == 0
    set(handles.listbox_mat_files,'Enable','off');
    set(handles.add_mat_button,'Enable','off');
    set(handles.text5,'String','');
    set(handles.listbox_mat_files,'String','');
    set(handles.pushbutton_remove,'Enable','off');
else
    listbox_param_files_Callback(hObject, eventdata, handles);
end




% --- Executes on selection change in listbox2.
function listbox_mat_files_Callback(hObject, eventdata, handles)
% hObject    handle to listbox2 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = get(hObject,'String') returns listbox2 contents as cell array
%        contents{get(hObject,'Value')} returns selected item from listbox2


% --- Executes during object creation, after setting all properties.
function listbox_mat_files_CreateFcn(hObject, eventdata, handles)
% hObject    handle to listbox2 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: listbox controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
set(hObject,'BackgroundColor','white');






% --- Executes on button press in add_mat_button.
function add_mat_button_Callback(hObject, eventdata, handles)
% hObject    handle to add_mat_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
global hy;
specific_selection = strcat(get(handles.text5,'String'), filesep, '*.mat');
str_File = uigetfile(specific_selection,'Select mat File');
if ~isnumeric(str_File)
    % need to convert this too
    ht = get(handles.tree,'UserData');
    str_List_old = ht.get(get(handles.text5,'String'));
    if (size(str_List_old,1) == 0)
        str_List_new = {str_File};
        set(handles.Go_plot_button,'Enable','on');
    else
        str_List_new = [cell(str_List_old); str_File];
    end
    ht.put(get(handles.text5,'String'), str_List_new);
    set(handles.tree,'UserData',ht);
    %str_List_cell = get(handles.listbox_mat_files,'UserData');
    %if isempty(str_Write)
    %    set(handles.listbox_mat_files,'Value',1);
    %else
    %    hy.put(get(handles.text5,'String'),str_Write);
    %end
    set(handles.listbox_mat_files,'String',str_List_new);
    %set(handles.listbox_mat_files,'UserData',str_List_cell);
    set(handles.remove_mat_button,'Enable','on');
    
end


% --- Executes on button press in remove_mat_button.
function remove_mat_button_Callback(hObject, eventdata, handles)
% hObject    handle to remove_mat_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
global hy;
index_selected = get(handles.listbox_mat_files,'Value');
%str_List_cell = get(handles.listbox_mat_files,'UserData');
list = get(handles.listbox_mat_files,'String');
remain = setdiff(1:size(list,1),[index_selected]);
list = list(remain,:);
%str_List_cell = str_List_cell(remain);
set(handles.listbox_mat_files,'String',list);
%set(handles.listbox_mat_files,'UserData',str_List_cell);
if length(list) == 0
    set(handles.remove_mat_button,'Enable','off');
    ht = get(handles.tree,'UserData');
    ht.remove(get(handles.text5,'String'));
    set(handles.tree,'UserData',ht);
    set(handles.Go_plot_button,'Enable','off');
    
    
else
    ht = get(handles.tree,'UserData');
    ht.put(get(handles.text5,'String'),list);
    set(handles.tree,'UserData',ht);
    
end
set(handles.listbox_mat_files,'Value',1.0);






%param_name = (list(index_selected,:));
%if ~(exist(param_name(1:end-2)) == 7)
%    mkdir(param_name(1:end-2));
%end
%cd(param_name(1:end-2));
%set(handles.listbox_mat_files,'Visible','on');
%set(handles.add_mat_button,'Visible','on');
%set(handles.text5,'String',list(index_selected,1:end-2));
%get(handles.curr_dir,'UserData')



function Simulation_end_time_Callback(hObject, eventdata, handles)
% hObject    handle to Simulation_end_time (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of Simulation_end_time as text
%        str2double(get(hObject,'String')) returns contents of Simulation_end_time as a double
end_time = str2num(get(handles.Simulation_end_time,'String'));
if ~isnumeric(end_time) || isempty(end_time) || end_time <= 0
    msgbox('Please define a simulation end time greater than 0','','error');
    return;
end



% --- Executes during object creation, after setting all properties.
function Simulation_end_time_CreateFcn(hObject, eventdata, handles)
% hObject    handle to Simulation_end_time (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes on button press in Set_time.
function Set_time_Callback(hObject, eventdata, handles)
% hObject    handle to Set_time (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
%end_time = get(handles.Simulation_end_time,'String');

%open the simulink model for a while


% --- Executes on button press in Sim_Matlab_button.
function Sim_Matlab_button_Callback(hObject, eventdata, handles)
% hObject    handle to Sim_Matlab_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

%checking if there is more than one parameter file
modelName = get(handles.edit_model,'String');
if isempty(modelName)
    msgbox('Please choose a simulation model','','error'); 
    return; 
end

%% upon pressing the Start Simulation Local, one should be sure that the
%% parameter file is only one. The Timer Function should not have the whole
%% list passed to. 
param_files = get(handles.listbox_param_files,'String');
if isempty(param_files)
    msgbox('Please add a parameter file to the list','','error');
    return;
elseif length(param_files) > 1
    index_selected = get(handles.listbox_param_files,'Value');
    if isempty(index_selected)
       msgbox('Please select a parameter file from the list','','error'); 
       return;
    end
    param_file = param_files{index_selected}
else
    param_file = param_files{1};
end

param_file_dir = param_file(1:end-2);

end_time = str2num(get(handles.Simulation_end_time,'String'));

if (end_time <= 0) 
    msgbox('Please define a simulation end time greater than 0','','error');
    return;
end
if isempty(end_time)
    msgbox('Please define a simulation end time greater than 0','','error');
    return;
end

%remove all the folders corresponding to param-files
param_files = get(handles.listbox_param_files,'String');
Save_Mat_File_Info(hObject, eventdata, handles);
for i = 1:length(param_files)
    str_app = strtrim(param_files{i});
    if (exist(str_app(1:end-2)) == 7)
        rmdir(str_app(1:end-2),'s');
    end
end
LED = 10;
for i = 1:LED
    LEDhandles(i)=findobj('Tag',['textLED' num2str(i)]);
    set(LEDhandles(i),'Visible','off');
end
set(handles.textLEDpercentDone,'Visible','off');

set(handles.simulation_status,'Visible','on');
set(handles.pushbutton_start,'Enable','off');
set(handles.Sim_Matlab_button,'Enable','off');
model_Name = get(handles.edit_model,'String');
if exist(param_file_dir) ~= 7
    mkdir(param_file_dir);            
end
newDir = param_file_dir;
copyfile(param_file,[newDir filesep param_file], 'f');
copyfile(model_Name,[newDir filesep model_Name], 'f');
%copyfile('timerFcn.m',[newDir filesep 'timerFcn.m'],'f');
%set(handles.Go_plot_button,'Enable','on');

%% creates the new file to specific folder assiciated to the param file
%%the current directory should be the one in which the Simulink Model was
%%selected
%curr_dir = pwd ;
curr_dir = get(handles.edit_model,'UserData');
params.modelName =  model_Name;
params.paramFile = param_file;
params.pwd_dir = [curr_dir filesep param_file_dir];
params.end_time = str2num(get(handles.Simulation_end_time,'String'));
params.current_dir = curr_dir;


%set up the sampling period
period = 1;
end_time = params.end_time;
%create a timer object here
T = timer('TimerFcn', {'timerFcn',end_time, {param_file}, handles.editLEDbkground, handles.textLEDpercentDone, curr_dir, handles.Go_plot_button, handles.pushbutton_start, handles.Sim_Matlab_button, handles.simulation_status, {handles.textLED1, handles.textLED2, handles.textLED3, handles.textLED4, handles.textLED5, handles.textLED6, handles.textLED7, handles.textLED8, handles.textLED9, handles.textLED10}, @stopTimer_Callback} ,'ExecutionMode','FixedRate','Period',period);
%starting the timer before executing 
start(T);

PAsolve_modelname(params);
    

function stopTimer_Callback(obj, curr_dir, Go_plot_button, pushbutton_start, Sim_Matlab_button, simulation_status)
    stop(obj);
    %set(handles.simulation_status,'String','the simulation ended ');
    %set(Go_plot_button,'Enable','on');
    %set(handles.simulation_status,'String','the simulations ended');
    set(pushbutton_start,'Enable','on');
    set(Sim_Matlab_button,'Enable','on');
    set(simulation_status,'Visible','on');
    file = [curr_dir filesep 'M_data.mat'];
    delete(file);


% --- Executes on button press in reset_button.
function reset_button_Callback(hObject, eventdata, handles)
% hObject    handle to reset_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)


%making buttons invisible

clear_plots_button_Callback(hObject, eventdata, handles);
set(handles.Go_plot_button,'Enable','off');
%deleting the model_name
set(handles.edit_model,'String','');

%deleting the param files
set(handles.listbox_param_files,'String',{});
set(handles.listbox_param_files,'Value',0);

%deleting the mat files
set(handles.listbox_mat_files,'String',{});
set(handles.listbox_mat_files,'Value',0);

set(handles.listbox_mat_files,'Enable','off');
set(handles.add_mat_button,'Enable','off');

%making the start buttons be visible again
set(handles.Sim_Matlab_button,'Visible','on');
set(handles.pushbutton_start,'Visible','on');
set(handles.Sim_Matlab_button,'Enable','on');
set(handles.pushbutton_start,'Enable','on');

%resetting the simulation time
set(handles.Simulation_end_time,'String','');
initial_dir = get(handles.edit_model,'UserData');
%if (max(size(initial_dir)) ~= 0)    
%    cd (initial_dir);
%end

%remove all the folders corresponding to param-files
param_files = get(handles.listbox_param_files,'String');
for i = 1:length(param_files)
    str_app = strtrim(param_files{i});
    if (exist(str_app(1:end-2)) == 7)
        rmdir(str_app(1:end-2),'s');
    end
end

set(handles.listbox_param_files,'String',{});


set(handles.clear_plots_button,'Enable','off');
LED = 10;
for i = 1:LED
    LEDhandles(i)=findobj('Tag',['textLED' num2str(i)]);
    set(LEDhandles(i),'Visible','off');
end
set(handles.textLEDpercentDone,'Visible','off');

%remove all the items in the hash map
ht = get(handles.tree,'UserData');
ht.clear();
set(handles.tree,'UserData',ht);
set(handles.text5,'String','');
set(handles.remove_mat_button,'Enable','off');
%delete the pilot mat-file

set(handles.pushbutton_start,'Visible','on','Enable','on');
clear_plots_button_Callback(hObject, eventdata, handles);
curr_dir = pwd;
set(handles.current_directory,'String',curr_dir);
%need to delete the file with the fullpath, as exists acts on all the files on the current path 
if exist('M_data.mat') == 2
    % load the file
    load M_data;
    % extract the directory    
    delete([M_data.curr_dir filesep 'M_data.mat']);
end

%determine if a timer is still active
%finding all the timers
timers = timerfindall;

%stop all the timers
for i = 1:size(timers,2)
    stop(timers(i));
end

delete(timers);
%set the *.mdl selection filter as default
ud.filter = 'mdl';
set(handles.edit_model,'UserData',ud);  


% --- Executes on button press in clear_plots_button.
function clear_plots_button_Callback(hObject, eventdata, handles)
% hObject    handle to clear_plots_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

mat_files = get(handles.listbox_mat_files,'String');
if length(mat_files) > 0
    %hide the button
    set(handles.clear_plots_button,'Enable','off');
    set(handles.Go_plot_button,'Enable','on');

    %determine the length of the mat_files box
    sze = length(get(handles.listbox_mat_files,'String'));
    for i = 1:sze
        try 
            close(num2str(i));
        catch ME
        end
    end
end



function edit3_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function edit3_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes on button press in change_dir.
function change_dir_Callback(hObject, eventdata, handles)
% hObject    handle to change_dir (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
curr = uigetdir;
if ~isnumeric(curr)
    set(handles.current_directory,'String',curr);

    cd (curr);
    set(handles.curr_dir,'UserData',pwd);
end



function edit4_Callback(hObject, eventdata, handles)
% hObject    handle to edit4 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit4 as text
%        str2double(get(hObject,'String')) returns contents of edit4 as a
%        double


% --- Executes during object creation, after setting all properties.
function edit4_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit4 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


function editLEDbkground_Callback(hObject, eventdata, handles)
% hObject    handle to editLEDbkground (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of editLEDbkground as text
%        str2double(get(hObject,'String')) returns contents of editLEDbkground as a double


% --- Executes during object creation, after setting all properties.
function editLEDbkground_CreateFcn(hObject, eventdata, handles)
% hObject    handle to editLEDbkground (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED1_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED1_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED2_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED2_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED3_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED3_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED4_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED4_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED5_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED5_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED6_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED6_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED7_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED7_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function textLED8_Callback(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of edit3 as text
%        str2double(get(hObject,'String')) returns contents of edit3 as a double


% --- Executes during object creation, after setting all properties.
function textLED8_CreateFcn(hObject, eventdata, handles)
% hObject    handle to edit3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

function Save_Mat_File_Info(hObject, eventdata, handles)
%function being called upon starting the simulation
%values to be stored: 
 
    %current directory
    M_data.curr_dir = get(handles.curr_dir,'UserData');
    
    %current simulation model
    M_data.model_Name = get(handles.edit_model,'String');
    
    %param files
    M_data.param_Files = get(handles.listbox_param_files,'String');
    
    %end time
    M_data.end_time = get(handles.Simulation_end_time,'String');
    
    %associate mat-files
    save M_data.mat M_data


% --- Executes on button press in Exit_Matlab.
function Exit_Matlab_Callback(hObject, eventdata, handles)
% hObject    handle to Exit_Matlab (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
exit




function continue_simulation(hObject, eventdata, handles)
% hObject    handle to resume_simulation (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

%should start a new timer
period = 1;

param_files = get(handles.listbox_param_files,'String');

end_time = str2num(get(handles.Simulation_end_time,'String'));
curr_dir = get(handles.curr_dir,'UserData');
set(handles.pushbutton_start,'Enable','off');
set(handles.Sim_Matlab_button,'Enable','off');

T = timer('TimerFcn', {'timerFcn',end_time, param_files, handles.editLEDbkground, handles.textLEDpercentDone, curr_dir, handles.Go_plot_button, handles.pushbutton_start, handles.Sim_Matlab_button, handles.simulation_status, {handles.textLED1, handles.textLED2, handles.textLED3, handles.textLED4, handles.textLED5, handles.textLED6, handles.textLED7, handles.textLED8, handles.textLED9, handles.textLED10}, @stopTimer_Callback} ,'ExecutionMode','FixedRate','Period',period);
%starting the timer before executing
%saving the mat-file

start(T);

function save_sampleTime_info(hObject, eventdata, handles)
modelName = get(handles.edit_model,'String');
modelName = modelName(1:end-4);
bdclose all
warning off all
set_param(0,'CharacterEncoding', 'ISO-8859-1');
load_system(modelName);
mat_blocks = find_system(modelName,'BlockType','ToFile');

if length(mat_blocks) > 0
    tol = zeros(1,length(mat_blocks));
    for i = 1:length(mat_blocks)
        tol(i) = str2num(get_param(mat_blocks{i},'Sample time'));
    end
    tolerance = min(tol);
    save(['tol.mat'], 'tolerance');
end
save_system(modelName);
close_system(modelName);


% --- Executes on button press in exit_button.
function exit_button_Callback(hObject, eventdata, handles)
% hObject    handle to exit_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
exit


