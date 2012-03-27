function opts = PAoptions(varargin)

    global ('proactive_pa_options', 'PA_scheduler_dir')             
    if  argn(2) == 0 & ~isempty(proactive_pa_options) then
        opts = proactive_pa_options; 
        return;
    elseif pmodulo(argn(2),2) ~= 0
        error(strcat(['Wrong number of arguments : ' string(nargin)]));
    end

    deff ("y=isnumeric(x)","y=or(type(x)==[1,5,8])","n");
    deff ("y=islogical(x)","y=or(type(x)==[4,6])","n");
    deff ("y=ischar(x)","y=type(x)==10","n");
    deff ("y=ismember(a,l)","y=(or(a==l))","n");
    deff ("y=logcheck(x)","if islogical(x), y=%t, elseif isnumeric(x), y=((x == 0)|(x == 1))elseif ischar(x), y=ismember(x,{''true'',''false''}), else y=%f, end","n");
    deff ("y=versioncheck(x)","if isnumeric(x), y=isempty(x), elseif ischar(x), y=~isempty(regexp(x, ''/^[1-9][0-9]*(\.[0-9]+)*$/'')), else y=%f, end","n");
    deff ("y=versionlistcheck(x)","if isnumeric(x), y=isempty(x), elseif ischar(x), y=~isempty(regexp(x, ''/^([1-9][0-9]*(\.[0-9]+)*[ ;,]+)*[1-9][0-9]*(\.[0-9]+)*$/'')), else y=%f, end","n");
    deff ("y=jarlistcheck(x)","if ischar(x), y=~isempty(regexp(x, ''/^([\w\-]+\.jar[ ;,]+)*[\w\-]+\.jar$/'')), else y=%f ,end","n");
    deff ("y=listcheck(x)","if ischar(x), y=~isempty(regexp(x, ''/^([^ ;,]+[ ;,]+)*[^ ;,]+$/'')), else y=%f ,end","n");

    listtrans = stringtolist;

    deff ("y=urlcheck(x)","if isnumeric(x), y=isempty(x), else y=ischar(x), end","n");
    deff ("y=charornull(x)","if isnumeric(x), y=isempty(x), else y=ischar(x), end","n");    


    v = getversion();
    v = strsubst(v,'scilab-','');
    majt = strtok(v,'.');
    mint = strtok('.');
    maint = strtok('.');  

    if ~exists('PA_scheduler_dir') 
        error('The environment variable SCHEDULER_HOME must be defined, use setenv to define it in Scilab');
    end
    fs = filesep();
    tmp_dir = system_getproperty('java.io.tmpdir');
    home_dir = system_getproperty('user.home');
    deff ("y=logtrans(x)","if islogical(x), y=x, elseif ischar(x), y=( x == ''on'' | x == ''true''), elseif isnumeric(x), y=(x==1), end","n");
    deff ("y=variabletrans(x)","y=strsubst(strsubst(strsubst(x, ''$SCHEDULER$'', PA_scheduler_dir),''$TMP$'',tmp_dir), ''$HOME$'', home_dir)","n");
    deff ("y=scripttrans(x)","y=strcat([''file:'', strsubst(variabletrans(x), ''\'', ''/'')])","n");
    deff ("y=conftrans(x)","y=strsubst(variabletrans(x),''/'',filesep())","n");

    deff ("y=ischarornull(x)","if isnumeric(x), y=isempty(x), else y=ischar(x), end","n");
    deff ("y=charornum(x)","y=or(type(x)==[1,5,8,10])","n");
    deff ("y=charornumtrans(x)","if isnumeric(x), y=x, else y=evstr(x), end","n");

    deff ("y=prioritycheck(x)","if ischar(x), y=ismember(x,{''Idle'', ''Lowest'', ''Low'', ''Normal'', ''High'', ''Highest''}), else y=%f, end","n")

    deff ("y=id(x)","y=x","n");

    j=1;
    inputs(j).name = 'JobName';
    inputs(j).default = 'Scilab';
    inputs(j).check = 'ischar';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'JobDescription';
    inputs(j).default = 'Set of parallel Scilab tasks';
    inputs(j).check = 'ischar';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'Debug';
    inputs(j).default = %f;
    inputs(j).check = 'logcheck';
    inputs(j).trans = 'logtrans';
    j=j+1;
    inputs(j).name = 'Fork';
    inputs(j).default = %f;
    inputs(j).check = 'logcheck';
    inputs(j).trans = 'logtrans';
    j=j+1;
    inputs(j).name = 'RunAsMe';
    inputs(j).default = %f;
    inputs(j).check = 'logcheck';
    inputs(j).trans = 'logtrans';
    j=j+1;
    inputs(j).name = 'RemoveJobAfterRetrieve';
    inputs(j).default = %f;
    inputs(j).check = 'logcheck';
    inputs(j).trans = 'logtrans';
    j=j+1;
    inputs(j).name = 'TransferEnv';
    inputs(j).default = %f;
    inputs(j).check = 'logcheck';
    inputs(j).trans = 'logtrans';    
    j=j+1;
    inputs(j).name = 'EnvExcludeList';
    inputs(j).default = 'demolist;scicos_pal;%scicos_menu;version;compiled;profilable;ans;called;%scicos_short;%helps;%helps_modules;MSDOS;who_user;%scicos_display_mode;%scicos_help';
    inputs(j).check = 'listcheck';
    inputs(j).trans = 'stringtocell';
    j=j+1;
    inputs(j).name = 'EnvExcludeTypeList';
    inputs(j).default = 'library;_Jobj;function';
    inputs(j).check = 'listcheck';
    inputs(j).trans = 'stringtocell';
    j=j+1;
    inputs(j).name = 'TransferVariables';
    inputs(j).default = %t;
    inputs(j).check = 'logcheck';
    inputs(j).trans = 'logtrans';
    j=j+1;
    inputs(j).name = 'CustomDataspaceURL';
    inputs(j).default = [];
    inputs(j).check = 'urlcheck';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'CustomDataspacePath';
    inputs(j).default = [];
    inputs(j).check = 'charornull';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'VersionPref';
    inputs(j).default = strcat([majt, '.', mint, '.', maint]);
    inputs(j).check = 'versioncheck';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'VersionRej';
    inputs(j).default = [];
    inputs(j).check = 'versionlistcheck';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'VersionMin';
    inputs(j).default = [];
    inputs(j).check = 'versioncheck';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'VersionMax';
    inputs(j).default = [];
    inputs(j).check = 'versioncheck';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'FindScilabScript';
    inputs(j).default = strcat(['$SCHEDULER$', fs, 'extensions', fs, 'scilab', fs, 'script', fs, 'file_scilab_finder.rb' ]);
    inputs(j).check = 'ischar';
    inputs(j).trans = 'scripttrans';
    j=j+1;
    inputs(j).name = 'CustomScript';
    inputs(j).default = [];
    inputs(j).check = 'ischarornull';
    inputs(j).trans = 'scripttrans';
    j=j+1;
    inputs(j).name = 'Priority';
    inputs(j).default = 'Normal';
    inputs(j).check = 'prioritycheck';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'WindowsStartupOptions';
    inputs(j).default = '-nw';
    inputs(j).check = 'ischarornull';
    inputs(j).trans = 'id';
    j=j+1;
    inputs(j).name = 'LinuxStartupOptions';
    inputs(j).default = '-nw';
    inputs(j).check = 'ischarornull';
    inputs(j).trans = 'id';
    j=j+1;
    //    inputs(j).name = 'ZipInputFiles';
    //    inputs(j).default = %f;
    //    inputs(j).check = 'logcheck';
    //    inputs(j).trans = 'logtrans';
    //    j=j+1;
    //    inputs(j).name = 'ZipOutputFiles';
    //    inputs(j).default = %f;
    //    inputs(j).check = 'logcheck';
    //    inputs(j).trans = 'logtrans';
    //    j=j+1;
    inputs(j).name = 'EmbeddedJars';
    inputs(j).default = 'ProActive_Scheduler-matsciemb.jar';
    inputs(j).check = 'jarlistcheck';
    inputs(j).trans = 'stringtocell';
    j=j+1;
    inputs(j).name = 'ProActiveJars';
    inputs(j).default = 'jruby.jar,jruby-engine.jar,jython.jar,jython-engine.jar,ProActive.jar,ProActive_Scheduler-client.jar,ProActive_SRM-common-client.jar,ProActive_Scheduler-matsci.jar';
    inputs(j).check = 'jarlistcheck';
    inputs(j).trans = 'stringtocell';
    j=j+1;
    inputs(j).name = 'ProActiveConfiguration';
    inputs(j).default = strcat(['$SCHEDULER$', fs, 'config', fs, 'proactive', fs, 'ProActiveConfiguration.xml']);
    inputs(j).check = 'ischar';
    inputs(j).trans = 'conftrans'
    j=j+1;
    inputs(j).name = 'Log4JConfiguration';
    inputs(j).default = strcat(['$SCHEDULER$', fs, 'config', fs, 'log4j', fs, 'log4j-client']);
    inputs(j).check = 'ischar';
    inputs(j).trans = 'conftrans';
    j=j+1;
    inputs(j).name = 'SecurityFile';
    inputs(j).default = strcat(['$SCHEDULER$', fs, 'config', fs, 'security.java.policy-client']);
    inputs(j).check = 'ischar';
    inputs(j).trans = 'conftrans';
    j=j+1;
    inputs(j).name = 'RmiPort';
    inputs(j).default = 1111;
    inputs(j).check = 'charornum';
    inputs(j).trans = 'charornumtrans';
    j=j+1;
    inputs(j).name = 'JvmTimeout';
    inputs(j).default = 1200;
    inputs(j).check = 'charornum';
    inputs(j).trans = 'charornumtrans';
    //    j=j+1;
    //    inputs(j).name = 'DisconnectedModeFile';
    //    inputs(j).default = strcat(['$HOME$', fs, '.PAsolveTmp.dat']);
    //    inputs(j).check = 'ischarornull';
    //    inputs(j).trans = 'conftrans';

    inlength = j; 

    // Parsing option file
    if isempty(proactive_pa_options) then 
        userdir = home_dir;
        optionpath = userdir + fs + '.scilab' + fs + 'PAoptions.ini'; 
        if isfile(optionpath) then
            [fid, ferr] = mopen(optionpath, 'r'); 
        else
            optionpath = fullfile(PA_scheduler_dir, 'extensions', 'scilab', 'config', 'toolbox', 'PAoptions.ini');
            if ~isfile(optionpath) then
                error(strcat(['Can''t locate options file at ""';optionpath;'"" , please make sure that SCHEDULER_HOME refers to the correct directory.']));
            end
            [fid, ferr] = mopen(optionpath, 'r');
        end    
        try

            [n,key, value] = getline(fid);

            //C = textscan(fid, '%s = %[^\n]', 'CommentStyle', '%');
            while n~=-1
                deblanked = stripblanks(key(1), %t);                                
                for j=1:inlength                          
                    if strcmp(deblanked,inputs(j).name) == 0 then
                        chk = inputs(j).check; 
                        tf = evstr(strcat([chk, '(value)']));                               
                        if ~tf then
                            disp(value)
                            error('Parse error when loading option file ' + optionpath + ', option ' + deblanked + ' doesn''t satisfy check '+ chk );
                        end
                        transfunc = inputs(j).trans;
                        def = evstr(strcat([transfunc, '(value)']))
                        proactive_pa_options(inputs(j).name) =  def;                   
                    end
                end
                [n,key, value] = getline(fid);
            end
        catch 
            mclose(fid);
            [str2,n2,line2,func2]=lasterror(%t);printf('!-- error %i %s at line %i of function %s',n2,str2,line2,func2);        
            error('Error while reading configuration file');
        end  
        mclose(fid);
    end    

    for i = 1:inlength
        default = %t;
        transfunc = inputs(i).trans;
        Parameter = inputs(i).default;
        for j= 1:argn(2)/2
            optionName = varargin(2*(j-1)+1);
            value = varargin(2*j);

            if inputs(i).name == optionName then
                chk = inputs(i).check;
                tf = evstr(strcat([chk, '(value)']));
                if ~tf then
                    disp(value)
                    error('Argument '+ optionName+ ' doesn''t satisfy check '+ chk );
                end
                default = %f;                    
                Parameter = evstr(strcat([transfunc, '(value)']));                                       
            end
        end

        if ~default | ~(isstruct(proactive_pa_options) & isfield(proactive_pa_options,inputs(i).name)) then      
            proactive_pa_options(inputs(i).name) =  Parameter;        
        end
    end


    opts = proactive_pa_options;    

endfunction

function [n,key,value]=getline(fid)
    [n,key, value] = mfscanf(fid, '%%%[^\n]')
    while n > 0
        [n,key, value] = mfscanf(fid, '%%%[^\n]') 
    end    
    [n,key, value] = mfscanf(fid, '%s = %[^\n]')
endfunction

function cl=stringtocell(x)
    cl=cell();
    i=1;
    remain=x;
    if iscell(x) then
        cl = x;
        return
    end
    goon=%t;
    str = strtok(remain, ',; ');
    while goon                          
        if isempty(str) | length(str) == 0 then
            goon=%f;
        else            
            cl(i).entries=str;
            i=i+1;
        end
        str = strtok(',; ');
    end
endfunction
function cl=stringtolist(x)
    cl=list();
    i=1;
    remain=x;
    if iscell(x) then
        cl = x;
        return
    end
    goon=%t;
    str = strtok(remain, ',; ');
    while goon                          
        if isempty(str) | length(str) == 0 then
            goon=%f;
        else            
            cl($+1)=str;
            i=i+1;
        end
        str = strtok(',; ');
    end
endfunction