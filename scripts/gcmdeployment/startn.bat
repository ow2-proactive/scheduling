@echo off
echo on
set nb=%1
set comm=%*
if %nb% LEQ 9 (
	set comm=%comm:~2%
) else (
  if %nb% LEQ 99 (
    set comm=%comm:~3%
 ) else (
    set comm=%comm:~4%
 )
)

for /L %%I IN (1,1,%nb%) DO (
echo %%I
start /b cmd /c %comm%
)
ENDLOCAL