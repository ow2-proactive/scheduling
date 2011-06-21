function checktoolboxes_start_and_hide_desktop(used_toolboxes, dir)

try
    if usejava('jvm')
        frames = java.awt.Frame.getFrames;
        awtinvoke(frames(1),'setVisible',0);
    end
catch
end

all_toolboxes = {'simulink', 'control', 'curvefit', 'images', 'compiler', 'nnet', 'optim', 'pde', 'robust', 'signal', 'slcontrol', 'spline', 'stats', 'symbolic', 'ident','toto'};
toolboxes_code = {'simulink', 'control_toolbox','curve_fitting_toolbox', 'image_toolbox','compiler', 'neural_network_toolbox', 'optimization_toolbox', 'pde_toolbox', 'robust_toolbox', 'signal_toolbox', 'simulink_control_design','spline_toolbox', 'statistics_toolbox', 'symbolic_toolbox', 'identification_toolbox','toto'};
i=1;
ok=true;
while i<=length(used_toolboxes) && ok
        [tf,pos]=ismember(used_toolboxes{i}, all_toolboxes);
        if tf
            ok = license('checkout', toolboxes_code{pos});
        end
        i=i+1;
end
if ok
    fack = fullfile(dir,'matlab.ack');
else
    fack = fullfile(dir,'matlab.nack');
end
fid = fopen(fack,'w');
fclose(fid);
if ~ok
    exit();
end