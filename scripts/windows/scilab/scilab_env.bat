REM Local deployment of Grid Scilab ToolBox
REM To define enviroment Variables
@echo off

#SET SCIDIR=<your current scilab path>
#SET SCI=<your current scilab path>
#SET PATH=%LD_LIBRARY_PATH%:%SCIDIR%/bin:.


SET SCIDIR="c:\scilab\build4\scilab"
SET SCI=%SCIDIR% 
SET PATH=%SCIDIR%\bin;%PATH%
