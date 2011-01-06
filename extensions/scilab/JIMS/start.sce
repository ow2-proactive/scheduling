here=get_absolute_file_path('start.sce');
macros=here+'macros';
java_home='/home/fviale/bin/jdk1.6.0_64/';
exec loader.sce;          
impjava=lib(macros);
if isfile('classpath/jlatexmath-0.9.3.jar') then
    javaclasspath([here+'jar/ScilabObjects.jar' here+'classpath' 'classpath/jlatexmath-0.9.3.jar' here+'classpath/scilatex.jar']);
else
    disp('Warning, cannot use latex library, in order to use it, download it at http://forge.scilab.org/index.php/p/jlatexmath/downloads/130/ and put the jar file into the classpath subfolder');
    javaclasspath([here+'jar/ScilabObjects.jar']);
end

