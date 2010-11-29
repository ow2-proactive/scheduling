function ok = mytransform(file)
%load('/user/fviale/home/matlab/format.mat')
%formats = imformats(format_struct);
newImg = imcomplement(imread(file));

[pathstr, name, ext, versn] = fileparts(file); 
newfile = fullfile(pathstr,[strcat('New_',name) '.pgm']);
imwrite(newImg, newfile);
ok = 1;

