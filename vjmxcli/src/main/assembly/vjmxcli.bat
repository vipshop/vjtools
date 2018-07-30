@echo off


rem check java
if "%JAVA_HOME%" == "" goto noJavaHome

set DIR=%~dp0
"%JAVA_HOME%\bin\java" %JAVA_OPTS% -cp "%DIR%/vjmxcli.jar;%JAVA_HOME%/lib/tools.jar" com.vip.vjtools.jmx.Client %*
goto end

:noJavaHome
  echo Please set JAVA_HOME before running this script
  goto end
:end

pause