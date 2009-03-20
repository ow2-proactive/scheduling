mode(-1)
toolboxname='PAscheduler'
pathB=get_absolute_file_path('buildmacros.sce')
disp('Building macros  in ' +pathB)
  tbx_build_macros(toolboxname,pathB);
  genlib(toolboxname+'lib',pathB,%t)
  clear pathB tbx_build_macros toolboxname;