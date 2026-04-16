@echo off
REM Launches IPOS-SA on Windows via the bundled Maven wrapper.
cd /d "%~dp0"
call mvnw.cmd -q clean javafx:run
