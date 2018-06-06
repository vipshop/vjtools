@echo off

rem check java
if "%JAVA_HOME%" == "" goto noJavaHome

rem get java version 
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
)

echo WARNING!! STW(Stop-The-World) will be performed on your Java process, if this is NOT wanted, type 'Ctrl+C' to exit.
echo using JDK %JAVAVER%

set BASEDIR=%~dp0
set SA_JDI_PATH=%JAVA_HOME%\lib\sa-jdi.jar
java -XX:AutoBoxCacheMax=20000 -classpath "%BASEDIR%\vjmap.jar;%SA_JDI_PATH%" com.vip.vjtools.vjmap.VJMap %*
goto end

:noJavaHome
  echo Please set JAVA_HOME before running this script
  goto end
:end

pause