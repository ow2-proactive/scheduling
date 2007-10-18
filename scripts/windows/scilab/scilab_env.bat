REM Local deployment of Grid Scilab ToolBox
REM To define enviroment Variables
@echo off

rem SET SCIDIR=<your current scilab path>
rem SET SCI=<your current scilab path>
rem SET MATLAB_DIR=<your current Matlab path>
rem SET PATH=%SCIDIR%\bin;%MATLAB_DIR%\bin\win32;%PROACTIVE%\lib;%PATH%

SET SCIDIR=c:\scilab\build4\scilab
SET SCI=%SCIDIR% 
SET MATLAB_DIR=C:\Program Files\MATLAB\R2007b
SET MATLAB=matlab.exe
SET PATH=%SCIDIR%\bin;%MATLAB_DIR%\bin\win32;%PROACTIVE%\lib;%PATH%
