rem ----------------------------------------------------------------------------
rem
rem This variable should be set to the directory where is installed ProActive
rem

IF NOT DEFINED PROACTIVE set PROACTIVE=..\..\.

rem ----------------------------------------------------------------------------


if NOT DEFINED JAVA_HOME goto javahome
if "%JAVA_HOME%" == "" goto javahome

rem ----
rem Set up the classpath using classes dir or jar files
rem 
set CLASSPATH=.
IF EXIST %PROACTIVE%\classes set CLASSPATH=%CLASSPATH%;%PROACTIVE%\classes
IF EXIST %PROACTIVE%\ProActive.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive.jar
IF EXIST %PROACTIVE%\ProActive_examples.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ProActive_examples.jar
IF EXIST %PROACTIVE%\lib\log4j.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\log4j.jar
IF EXIST %PROACTIVE%\lib\bouncycastle.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\bouncycastle.jar
IF EXIST %PROACTIVE%\lib\xercesImpl.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\xercesImpl.jar
IF EXIST %PROACTIVE%\lib\components\fractal.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\fractal.jar
IF EXIST %PROACTIVE%\lib\jsch.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jsch.jar
IF EXIST %PROACTIVE%\ic2d.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\ic2d.jar
IF EXIST %PROACTIVE%\lib\javassist.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\javassist.jar

rem -------------------------------------------------
rem jars for Jini
rem -------------------------------------------------
IF EXIST %PROACTIVE%\lib\jini\jini-core.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jini\jini-core.jar
IF EXIST %PROACTIVE%\lib\jini\jini-ext.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jini\jini-ext.jar
IF EXIST %PROACTIVE%\lib\jini\reggie.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\jini\reggie.jar

rem -------------------------------------------------
rem jars for Globus
rem -------------------------------------------------
IF EXIST %PROACTIVE%\lib\globus\cog-jglobus.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\globus\cog-jglobus.jar
IF EXIST %PROACTIVE%\lib\globus\cog-ogce.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\globus\cog-ogce.jar
IF EXIST %PROACTIVE%\lib\globus\cryptix.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\globus\cryptix.jar
IF EXIST %PROACTIVE%\lib\globus\cryptix32.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\globus\cryptix32.jar
IF EXIST %PROACTIVE%\lib\globus\cryptix-asn1.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\globus\cryptix-asn1.jar
IF EXIST %PROACTIVE%\lib\globus\puretls.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\globus\puretls.jar

rem -------------------------------------------------
rem jars for Ibis
rem -------------------------------------------------
IF EXIST %PROACTIVE%\lib\ibis\ibis.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ibis\ibis.jar

rem -------------------------------------------------
rem jars for Fractal GUI
rem -------------------------------------------------
IF EXIST %PROACTIVE%\lib\components\fractal-adl.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\fractal-adl.jar
IF EXIST %PROACTIVE%\lib\components\dtdparser.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\dtdparser.jar
IF EXIST %PROACTIVE%\lib\components\asm-2.2.1.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\asm-2.2.1.jar
IF EXIST %PROACTIVE%\lib\components\ow_deployment_scheduling.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\ow_deployment_scheduling.jar
IF EXIST %PROACTIVE%\lib\components\fractal-gui.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\fractal-gui.jar
IF EXIST %PROACTIVE%\lib\components\fractal-swing.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\fractal-swing.jar
IF EXIST %PROACTIVE%\lib\components\julia-asm.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\julia-asm.jar
IF EXIST %PROACTIVE%\lib\components\julia-mixins.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\julia-mixins.jar
IF EXIST %PROACTIVE%\lib\components\julia-runtime.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\julia-runtime.jar
IF EXIST %PROACTIVE%\lib\components\SVGGraphics.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\components\SVGGraphics.jar

rem -------------------------------------------------
rem jars for Web Services
rem -------------------------------------------------
IF EXIST %PROACTIVE%\lib\ws\soap.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\soap.jar
IF EXIST %PROACTIVE%\lib\ws\wsdl4j.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\wsdl4j.jar
IF EXIST %PROACTIVE%\lib\ws\axis.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\axis.jar
IF EXIST %PROACTIVE%\lib\ws\jaxrpc.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\jaxrpc.jar
IF EXIST %PROACTIVE%\lib\ws\activation.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\activation.jar
IF EXIST %PROACTIVE%\lib\ws\saaj-api.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\saaj-api.jar
IF EXIST %PROACTIVE%\lib\ws\commons-logging.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\commons-logging.jar
IF EXIST %PROACTIVE%\lib\ws\commons-discovery.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\commons-discovery.jar
IF EXIST %PROACTIVE%\lib\ws\mail.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\mail.jar
IF EXIST %PROACTIVE%\lib\ws\xml-apis.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\xml-apis.jar

rem -------------------------------------------------
rem jars for Web Services
rem -------------------------------------------------
IF EXIST %PROACTIVE%\lib\ws\soap.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\soap.jar
IF EXIST %PROACTIVE%\lib\ws\wsdl4j.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\wsdl4j.jar
IF EXIST %PROACTIVE%\lib\ws\axis.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\axis.jar
IF EXIST %PROACTIVE%\lib\ws\jaxrpc.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\jaxrpc.jar
IF EXIST %PROACTIVE%\lib\ws\activation.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\activation.jar
IF EXIST %PROACTIVE%\lib\ws\saaj-api.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\saaj-api.jar
IF EXIST %PROACTIVE%\lib\ws\commons-logging.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\commons-logging.jar
IF EXIST %PROACTIVE%\lib\ws\commons-discovery.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\commons-discovery.jar
IF EXIST %PROACTIVE%\lib\ws\mail.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\mail.jar
IF EXIST %PROACTIVE%\lib\ws\xml-apis.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\ws\xml-apis.jar

rem -------------------------------------------------
rem jars for TimIt
rem -------------------------------------------------
IF EXIST %PROACTIVE%\lib\timit\batik-awt-util.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\batik-awt-util.jar
IF EXIST %PROACTIVE%\lib\timit\batik-dom.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\batik-dom.jar
IF EXIST %PROACTIVE%\lib\timit\batik-svggen.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\batik-svggen.jar
IF EXIST %PROACTIVE%\lib\timit\batik-util.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\batik-util.jar
IF EXIST %PROACTIVE%\lib\timit\batik-xml.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\batik-xml.jar
IF EXIST %PROACTIVE%\lib\timit\commons-cli-1.0.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\commons-cli-1.0.jar
IF EXIST %PROACTIVE%\lib\timit\jcommon-1.0.6.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\jcommon-1.0.6.jar
IF EXIST %PROACTIVE%\lib\timit\jdom.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\jdom.jar
IF EXIST %PROACTIVE%\lib\timit\jfreechart-1.0.2.jar set CLASSPATH=%CLASSPATH%;%PROACTIVE%\lib\timit\jfreechart-1.0.2.jar


rem echo CLASSPATH=%CLASSPATH%

set JAVA_CMD="%JAVA_HOME%\bin\java.exe" -Djava.security.manager -Djava.security.policy=%PROACTIVE%\scripts\proactive.java.policy -Dlog4j.configuration=file:%PROACTIVE%\scripts\proactive-log4j
set PATH=%JAVA_HOME%\bin;%PATH%
goto end


:javahome
echo.
echo The enviroment variable JAVA_HOME must be set the current jdk distribution
echo installed on your computer.
echo Use 
echo    set JAVA_HOME=<the directory where is the JDK>
goto end

 
:end
