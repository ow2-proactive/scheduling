function keepalive_callback_fcn(obj, event, used_toolboxes)

all_toolboxes = {'simulink', 'control', 'curvefit', 'images', 'compiler', 'nnet', 'optim', 'pde', 'robust', 'signal', 'slcontrol', 'spline', 'stats', 'symbolic', 'ident'};
toolboxes_code = {'simulink', 'control_toolbox','curve_fitting_toolbox', 'image_toolbox','compiler', 'neural_network_toolbox', 'optimization_toolbox', 'pde_toolbox', 'robust_toolbox', 'signal_toolbox', 'simulink_control_design','spline_toolbox', 'statistics_toolbox', 'symbolic_toolbox', 'identification_toolbox'};

for i=1:length(used_toolboxes)
    [tf,pos]=ismember(used_toolboxes{i}, all_toolboxes);
    if tf
        license('checkout', toolboxes_code{pos});
    end
end
