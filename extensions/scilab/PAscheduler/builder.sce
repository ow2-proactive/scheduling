mode(-1);
lines(0);
global PA_scheduler_dir

try
    getversion('scilab');
catch
    error(gettext('Scilab 5.0 or more is required.'));
end;
if ~exists('PA_scheduler_dir') | PA_scheduler_dir == []
            oldcd=pwd();
            cd(strcat(['..', filesep(),'..',filesep(),'..']));
            PA_scheduler_dir = pwd();
            cd(oldcd);
            //error('The environment variable SCHEDULER_HOME must be defined, use setenv to define it in Scilab');

    end
// ====================================================================
//if ~with_module('development_tools') then
//    error(msprintf(gettext('%s module not installed."),'development_tools'));
//end
here=get_absolute_file_path('builder.sce');
macros=here+'macros';


//ilib_build('importjava',sci_gtw,[cpp c jni gateway],[],'','-ljvm',include);

genlib('toolbox_pascheduler',macros);
