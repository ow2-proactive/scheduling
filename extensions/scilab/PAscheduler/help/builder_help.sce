mode(-1) //force silent execution

help_lang_dir = get_absolute_file_path('builder_help.sce');

tbx_builder_help_lang("en_US", help_lang_dir);

clear help_lang_dir;

  //  path=get_absolute_file_path('buildhelp.sce'); // get the absolute path of this file
  //  add_help_chapter("PAscheduler",path); // add help chapter
  //  xmltohtml(path,"PAscheduler")
    //clear the variable stack
 //   clear path add_help_chapter get_absolute_file_path