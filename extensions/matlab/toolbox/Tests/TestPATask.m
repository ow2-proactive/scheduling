% /*
%   * ################################################################
%   *
%   * ProActive Parallel Suite(TM): The Java(TM) library for
%   *    Parallel, Distributed, Multi-Core Computing for
%   *    Enterprise Grids & Clouds
%   *
%   * Copyright (C) 1997-2011 INRIA/University of
%   *                 Nice-Sophia Antipolis/ActiveEon
%   * Contact: proactive@ow2.org or contact@activeeon.com
%   *
%   * This library is free software; you can redistribute it and/or
%   * modify it under the terms of the GNU Affero General Public License
%   * as published by the Free Software Foundation; version 3 of
%   * the License.
%   *
%   * This library is distributed in the hope that it will be useful,
%   * but WITHOUT ANY WARRANTY; without even the implied warranty of
%   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
%   * Affero General Public License for more details.
%   *
%   * You should have received a copy of the GNU Affero General Public License
%   * along with this library; if not, write to the Free Software
%   * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
%   * USA
%   *
%   * If needed, contact us to obtain a release under GPL Version 2 or 3
%   * or a different license than the AGPL.
%   *
%   *  Initial developer(s):               The ProActive Team
%   *                        http://proactive.inria.fr/team_members.htm
%   *  Contributor(s):
%   *
%   * ################################################################
%   * $$PROACTIVE_INITIAL_DEV$$
%   */
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

T = PATask(1,numFrames);
for p = 1:numFrames

    T(1,p).Compose = false;
    T(1,p).Params = {['images_low' '/' fileNames{p}]};
    T(1,p).Func = @mytransform;
    T(1,p).InputFiles = {['images_low' '/' fileNames{p}]};
    [pathstr, name, ext] = fileparts(fileNames{p});
    T(1,p).OutputFiles = {['images_low' '/' 'New_' name '.pgm']};
    T(1,p).Description = ['Image recogition' num2str(p)];
    [pathstr, name, ext] = fileparts(mfilename('fullpath'));
    T(1,p).SelectionScript = [pathstr filesep 'script' filesep 'rand_script.rb' ];
end
% Prepare

T

disp('...... Testing PAsolve with image processing, input/output files, and custom selection script');
resl = PAsolve(T);
val=PAwaitFor(resl,timeout)

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

