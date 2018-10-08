@echo off

rem check java
if "%JAVA_HOME%" == "" goto noJavaHome


echo WARNING!! STW(Stop-The-World) will be performed on your Java process, if this is NOT wanted, type 'Ctrl+C' to exit.


set DIR=%~dp0
set JAVA_OPTS="-Xms512m -Xmx512m -Xmn400m -XX:+TieredCompilation -XX:+UseConcMarkSweepGC -Xverify:none -XX:AutoBoxCacheMax=20000"


"%JAVA_HOME%\bin\java" %JAVA_OPTS% -classpath "%DIR%\vjmap.jar;%JAVA_HOME%\lib\sa-jdi.jar" com.vip.vjtools.vjmap.VJMap %*
goto end

:noJavaHome
  echo Please set JAVA_HOME before running this script
  goto end
:end

pause