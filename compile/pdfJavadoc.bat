@echo off

if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

SETLOCAL
set CLASSPATH=mifdoclet.jar;..\classes;..\lib\bouncycastle.jar;..\lib\crimson.jar;..\lib\cog.jar;..\lib\javaxCrypto.jar;..\lib\jaxp.jar;..\lib\jini-core.jar;..\lib\jini-ext.jar;..\lib\reggie.jar;..\lib\bcel.jar;..\lib\asm.jar;..\lib\sun-util.jar;..\lib\cryptix.jar;..\lib\iaik_jce_full.jar;..\lib\iaik_ssl.jar;..\lib\log4j.jar
echo %CLASSPATH%

echo Filtering html doc-files...

%JAVA_HOME%\bin\java util.FilterDocFiles ..\src\org\objectweb\proactive\doc-files

echo Copying image files to compile directory to fix bug with MIFDoclet ...
copy ..\src\org\objectweb\proactive\doc-files\*.gif ..\compile


echo Producing javadoc with MIFDoclet

%JAVA_HOME%\bin\javadoc ^
   -doclet com.sun.tools.doclets.mif.MIFDoclet ^
   -docletpath .\mifdoclet.jar ^
   -J-Xmx64M ^
   -batch D:\java\dzbatcher\bin\dzbatcher ^
   -book ProActiveBook.xml ^
   -sourcepath ..\src ^
   -group "ProActive packages for end user" "org.objectweb.proactive:org.objectweb.proactive.rmi" ^
   -group "ProActive Core" "org.objectweb.proactive.core*" ^
   -group "ProActive Core : Body" "org.objectweb.proactive.core.body*" ^
   -group "ProActive Extensions" "org.objectweb.proactive.ext*" ^
   -group "ProActive Extensions : Security (*under development*)" "org.objectweb.proactive.ext.security*" ^
   -group "IC2D Application : Interactive Control and Debugging of Distribution" "org.objectweb.proactive.ic2d*" ^
   @packages.txt


echo Removing filtered html doc-files...
del ..\src\org\objectweb\proactive\doc-files\_*.*

echo Removing copied images...
del ..\compile\*.gif

ENDLOCAL
goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set to the current jdk 
echo distribution installed on your computer.
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end


:end

