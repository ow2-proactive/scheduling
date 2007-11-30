REM Local deployment of Grid Scilab ToolBox
REM To define enviroment Variables
@echo off

REM ######### Scilab use ##########

REM SET SCIDIR=<your current scilab path>
SET SCIDIR=c:\scilab\build4\scilab
SET SCI=%SCIDIR%

REM #### Scilab libraries
REM SET PATH=%PATH%;<path to Scilab libraries>
SET PATH=%PATH%;%SCIDIR%\bin

REM ######### Matlab use ##########

REM SET MATLAB_DIR=<your current matlab installation path>
SET MATLAB_DIR=c:\Program Files\Mathworks\Matlab2006b

REM SET MATLAB=<the name of your matlab command> 
SET MATLAB=matlab.exe

# REM SET PROACTIVE=<the path to your ProActive installation>
SET PROACTIVE=c:\ProActive


REM #### Matlab libraries
REM #### In order to find the right paths, refer to :
REM # http://www.mathworks.com/access/helpdesk/help/techdoc/index.html?/access/helpdesk/help/techdoc/matlab_external/f39903.html&http://www.mathworks.com/access/helpdesk/help/techdoc/helptoc.html
SET PATH=%PATH%:%MATLAB_DIR%\bin\win32:%MATLAB_DIR%\sys\os\win32

REM # The script which builds the Matlab<->Java interface copies the library into $PROACTIVE/lib/<Matlab_version>/<matlab relative path to main libraries>
REM # So in this example (version 7.3) that would be:
SET PATH=%PATH%:%PROACTIVE%\lib\7.3\bin\win32

