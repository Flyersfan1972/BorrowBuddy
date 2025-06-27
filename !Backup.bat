@echo off
set /p commit_message=Enter commit message: 

:: Perform Git backup
git add .
git commit -m "%commit_message%"
git push origin master

:: Get current date in MM_DD_YYYY format
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "MM=%dt:~4,2%"
set "DD=%dt:~6,2%"
set "YYYY=%dt:~0,4%"
set "date_folder=%MM%_%DD%_%YYYY%"

:: Copy project to dated folder
set "source=C:\Code\Android\BorrowBuddy"
set "destination=C:\Code\Android\BorrowBuddy - %date_folder%"
xcopy "%source%" "%destination%" /E /I /Y

echo Backup to GitHub and local folder (%destination%) complete! Press any key to exit.
pause