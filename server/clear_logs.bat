@echo off
set "logDir=logs"
del /Q "%logDir%\*.gz"
echo All the log-files in "%logDir%" have been removed.
