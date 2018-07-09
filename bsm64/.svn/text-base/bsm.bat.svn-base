@echo off
rem Starten des BSM Rechenkerns
rem Parameter:
rem --batch (optional): Aufruf als Batch aus Excel, sonst als Dialog
rem Verzeichnis (optional): Verzeichnis des Excel-Blattes, sonst wird das aktuelle Verzeichnis verwendet

rem das Verzeichnis dieses Start-Scripts:
set BASE=%~dp0

rem Start des Programms:
"%BASE%\jre\bin\javaw.exe" -cp "%BASE%\jar\bsm.jar" -splash:"%BASE%etc\splash.gif" de.gdv.bsm.intern.applic.Applic %1 %2 

exit /B %ERRORLEVEL%
