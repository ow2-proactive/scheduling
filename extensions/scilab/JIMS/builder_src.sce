mode(-1);
here=get_absolute_file_path('builder_src.sce');
fs = filesep();

if (getos() == "Windows") then
    fsfs= fs+fs;
else
    fsfs=fs;
end

macros=here+'macros';
cpp=ls(here+fs+'cpp'+fs+'*.cpp')';
c=ls(here+fs+'c'+fs+'*.c')';
jni=ls(here+fs+'jni'+fs+'*.cpp')';
gateway=ls(here+fs+'sci_gateway'+fs+'*.c')';
for ff=cpp
    copyfile(ff,'.')
end
for ff=c
    copyfile(ff,'.')
end
for ff=jni
    copyfile(ff,'.')
end
for ff=gateway
    copyfile(ff,'.')
end

acpp=ls('*.cpp')';
ac=ls('*.c')';
ac=setdiff(ac,'importjava.c');

java_home=getenv('JAVA_HOME');

if (getos() == "Windows") then
    if win64() then
    else
        include='-I""'+here+fs+'include"" -I""'+java_home+fs+'include""'+..
        ' -I""'+java_home+fs+'include'+fs+'win32""' + ' -I""' + SCI + fs+'modules'+fs+'jvm'+fs+'includes""'+..
	' -L""'+java_home+fs+'jre'+fs+'bin'+fs+'client'+'""'+..
	' -L""'+java_home+fs+'jre'+fs+'bin'+fs+'server'+'""';
        
    end
else
    include='-I""'+here+fs+'include"" -I""'+java_home+fs+'include""'+..
        ' -I""'+java_home+fs+'include'+fs+'linux""' + ' -I""' + SCI + fs+'modules'+fs+'jvm'+fs+'includes""'+..
	' -L""'+java_home+fs+'jre'+fs+'lib'+fs+'amd64'+fs+'client'+fs+'""'+..
	' -L""'+java_home+fs+'jre'+fs+'lib'+fs+'amd64'+fs+'server'+fs+'""';
end



sci_gtw=['loadClass' 'sci_loadClass';
         'javadeff' 'sci_javadeff';
         'javaCast' 'sci_javaCast';
	 'javaArray' 'sci_javaArray';		
	 'wrapvar' 'sci_wrapvar';
	 'wrap' 'sci_wrap';
	 'wrapinfloat' 'sci_wrapinfloat';
	 'wrapinchar' 'sci_wrapinchar';
	 'convMatrixMethod' 'sci_convMatrixMethod';
	 'import' 'sci_import';
	 'import_u' 'sci_import_u';
	 'newInstance' 'sci_newInstance';
	 'newInstance_l' 'sci_newInstance_l';
	 'invoke' 'sci_invoke';
	 'setField' 'sci_setfield';
	 'setField_l' 'sci_setfield_l';
	 'getField' 'sci_getfield';
	 'getField_l' 'sci_getfield_l';
	 'getField_u' 'sci_getfield_u';
	 'getField_lu' 'sci_getfield_lu';
	 'getFields' 'sci_getFields';
	 'invoke_u' 'sci_invoke_u';
	 'invoke_l' 'sci_invoke_l';
	 'invoke_lu' 'sci_invoke_lu';
	 'unwrap' 'sci_unwrap';
	 'remove' 'sci_remove';
	 'getMethods' 'sci_getMethods';
	 'getRep' 'sci_getRep']

ilib_build('importjava',sci_gtw,[acpp ac],[],'','-ljvm',include);

for ff=acpp
    try
        mdelete(ff)
    catch 
    end
end

for ff=ac
    try
        mdelete(ff)
    catch 
    end
end

ao = ls('*.o');

for ff=ao
    try
        mdelete(ff)
    catch 
    end
end


genlib('impjava',macros);
