function res = PAsolve_modelname(params)
bdclose all
warning off all
rightpwd = params.pwd_dir; 
cd(rightpwd)
%pwd
set_param(0,'CharacterEncoding', 'ISO-8859-1');
assignin('base','params',params);
assignin('base','str_cur_param',params.pwd_dir);
evalin('base',[params.pwd_dir '()']);
options = simset('SrcWorkspace', 'base', 'DstWorkspace','base');
modelName = params.modelName;
modelName = modelName(1:end-4);
load_system(modelName);
set_param(modelName,'StopTime',num2str(params.end_time));
%saving a file containing the infomation about the tolerance
mat_blocks = find_system(modelName,'BlockType','ToFile');
%if length(mat_blocks) > 0
%    for i = 1:length(mat_blocks)
%        tol(i) = get_param(mat_blocks{i},'Sample time')
%    end
%    tolerance = min(tol);
%    save([params.current_dir filesep 'tol.mat'], 'tolerance');
%end
    


save_system(modelName);
close_system(modelName);

sim(params.modelName,[],options);
    
bdclose all
res = 1;
