//libs = "";
    //       ldflags = [];

    //       ilib_for_link('PAschedulersrc','ScilabEmbeddedc.c',libs,"c");

tbx_build_src(['ScilabEmbeddedc'], ['ScilabEmbeddedc.c'], 'c', ..
                  get_absolute_file_path('builder_c.sce'));


clear tbx_build_src;

