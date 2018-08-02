@echo off

rem check java
if "%JAVA_HOME%" == "" goto noJavaHome


echo WARNING!! STW(Stop-The-World) will be performed on your Java process, if this is NOT wanted, type 'Ctrl+C' to exit.


set DIR=%~dp0
set JAVA_OPTS="-Xms256m -Xmx256m -XX:NewRatio=1 -Xss256k -XX:ReservedCodeCacheSize=72M -XX:+UseSerialGC -Xverify:none -XX:AutoBoxCacheMax=20000"


java %JAVA_OPTS% -classpath "%DIR%\vjmap.jar;%JAVA_HOME%\lib\sa-jdi.jar" com.vip.vjtools.vjmap.VJMap %*
goto end

:noJavaHome
  echo Please set JAVA_HOME before running this script
  goto end
:end

pause