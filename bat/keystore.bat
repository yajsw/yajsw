cd %~dp0
call setenv.bat
set
%wrapper_bat% -k %1 %2
pause
