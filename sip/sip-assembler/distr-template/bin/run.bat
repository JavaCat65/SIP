@echo off

if "%JAVA_HOME%" == "" goto java_home_not_found
echo JAVA_HOME = %JAVA_HOME%

"%JAVA_HOME%/bin/java.exe" -version
echo Starting SIP...
set sipHomeDir=%~dp0..\
set sipLogConfig=%sipHomeDir%conf\log4j2.xml

"%JAVA_HOME%/bin/java.exe"^
    "-Dsip.home.dir=%sipHomeDir% "^
    "-Dlog4j.configurationFile=%sipLogConfig% "^
    -jar boot.jar 2>&1

goto:eof

:java_home_not_found
   Echo %JAVA_HOME% not found
goto:eof