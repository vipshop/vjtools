@echo off


rem check java
if "%JAVA_HOME%" == "" goto noJavaHome


set DIR=%~dp0
set JAVA_OPTS="-Xms96m -Xmx96m -Xmn64m -Xss256k -XX:+UseSerialGC -Djava.compiler=NONE -Xverify:none -XX:AutoBoxCacheMax=20000"

"%JAVA_HOME%\bin\java" %JAVA_OPTS% -cp "%DIR%/vjmxcli.jar;%JAVA_HOME%/lib/tools.jar" com.vip.vjtools.jmx.Client %*
goto end

:noJavaHome
  echo Please set JAVA_HOME before running this script
  goto end
:end

pause