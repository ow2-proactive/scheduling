@echo off
echo. 
echo --- C3D ---------------------------------------------

goto doit

:usage
echo. 
goto end


:doit
SETLOCAL
call init.bat
start %JAVA_CMD% org.objectweb.proactive.StartNode //localhost/Renderer1
start %JAVA_CMD% org.objectweb.proactive.StartNode //localhost/Renderer2
start %JAVA_CMD% org.objectweb.proactive.StartNode //localhost/Renderer3
start %JAVA_CMD% org.objectweb.proactive.StartNode //localhost/Renderer4

%JAVA_CMD% org.objectweb.proactive.examples.matrix.Main c3d.hosts 300
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
