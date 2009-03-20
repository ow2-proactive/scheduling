
//ilib_name = 'libPAscheduler'		// interface library name

//       files=['scilabembedded.c'];


//       libs=["../src/libPAschedulersrc"];

       table =["initEmbedded","sci_initEmbedded";
           "connect","sci_connect";
           "sciSolve","sci_sciSolve"];   // table of (scilab_name,interface-name)

//       makename = "";

//       ldflags = "";

//       ilib_build(ilib_name,table,files,libs, makename,ldflags);


tbx_build_gateway('PAscheduler', table, ['sci_scilabembedded.c'], ..
                         get_absolute_file_path('builder_gateway_c.sce'), ..
                         ['../../src/c/libScilabEmbeddedc']);

clear tbx_build_gateway;

