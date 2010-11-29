function [ok, msg]=TestPATask(timeout)
if ~exist('timeout', 'var')
    if ispc()
        timeout = 400000;
    else
        timeout = 200000;
    end
end
oldpwd = pwd;
% Create an array of filenames that make up the image sequence
[pathstr, name, ext] = fileparts(mfilename('fullpath'));
cd(pathstr);
fileFolder = [pathstr filesep 'images_low'];
dirOutput = dir(fullfile(fileFolder,'AT3_1m4_*.tif'));
fileNames = {dirOutput.name}';
numFrames = numel(fileNames);
delete([fileFolder '/' 'New*.pgm']);

T(1:numFrames) = PATask;
for p = 1:numFrames

    T(p).Compose = false;
    T(p).Params = {['images_low' '/' fileNames{p}]};
    T(p).Func = @mytransform;
    T(p).InputFiles = {['images_low' '/' fileNames{p}]};
    [pathstr, name, ext] = fileparts(fileNames{p});
    T(p).OutputFiles = {['images_low' '/' 'New_' name '.pgm']};
    T(p).Description = ['Image recogition' num2str(p)];
    [pathstr, name, ext] = fileparts(mfilename('fullpath'));
    T(p).SelectionScript = [pathstr filesep 'script' filesep 'rand_script.rb' ];
end
% Prepare

T

disp('...... Testing PAsolve with image processing, input/output files, and custom selection script');
resl = PAsolve(T);
val=PAwaitAll(resl,timeout)

for i=1:length(val)
    if val{i} ~= 1
        ok=false;
        msg='TestPATask::Some tasks didn''t succeed';
        return;
    end
end

% View results
h=figure;
for k = 1:numFrames
    imshow(imread(['images_low' filesep fileNames{p}]),'InitialMagnification', 30);
    title(sprintf('Original Image # %d',k));
    pause(0.3);
    [pathstr, name, ext] = fileparts(fileNames{p});
    newfile = fullfile(pathstr,['images_low' filesep 'New_' name '.pgm']);
    imshow(imread(newfile),[],'InitialMagnification', 30);
    title(sprintf('Processed Image # %d',k));
    pause(1);
end
close(h);

cd(oldpwd);
ok=true;
msg=[];

