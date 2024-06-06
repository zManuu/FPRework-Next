@echo off
set "logDir=logs"
del /Q "%logDir%\*.gz"
echo Alle .gz-Dateien im Ordner %logDir% wurden gelöscht.
