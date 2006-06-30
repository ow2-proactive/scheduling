REM Local deployment of Grid Scilab ToolBox
REM To define enviroment Variables
@echo off

SET SCIDIR="c:\scilab\build4\scilab"
SET SCI=%SCIDIR% 
SET PATH=%SCIDIR%\bin;%PATH%
SET CLASSPATH=%SCIDIR%\javasci.jar;%CLASSPATH%
