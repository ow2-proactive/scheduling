mode(-1);
lines(0);
global PA_scheduler_dir

// Version Check
try
    version = getversion('scilab');
    if version(1) < 5
        error(gettext('Scilab 5.3 or more is required.'));
    elseif version(1) == 5 & version(2) < 3
        error(gettext('Scilab 5.3 or more is required.'));
    end
catch
    error(gettext('Scilab 5.3 or more is required.'));
end;

// JIMS install check
if ~atomsIsInstalled('JIMS')
    error('JIMS Atoms Module must be installed in order to use ProActive Scheduler Module');
end

disp('Start ProActive');

root_tlbx = get_absolute_file_path('builder.sce');
if ~exists('PA_scheduler_dir') | PA_scheduler_dir == []    
    oldcd=pwd();
    cd(root_tlbx);
    PA_scheduler_dir=fullpath('..'+filesep()+'..'+filesep()+'..');
    cd(oldcd);
            //error('The environment variable SCHEDULER_HOME must be defined, use setenv to define it in Scilab');
        
end
// ====================================================================
//if ~with_module('development_tools') then
//    error(msprintf(gettext('%s module not installed."),'development_tools'));
//end
macros=root_tlbx+'macros';


//ilib_build('importjava',sci_gtw,[cpp c jni gateway],[],'','-ljvm',include);

genlib('toolbox_pascheduler',macros);
