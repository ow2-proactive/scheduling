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
pause
start %JAVA_CMD% org.objectweb.proactive.examples.c3d.C3DDispatcher c3d.hosts
ENDLOCAL

:end
echo. 
echo ---------------------------------------------------------
