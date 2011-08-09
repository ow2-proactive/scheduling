SET file=%0
SET DIR=%file%\..
echo Process tree killer test : detached command

cmd /C "%DIR%\TestSleep.exe"