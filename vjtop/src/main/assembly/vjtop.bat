@echo off
set DIR=%~dp0

"%JAVA_HOME%\bin\java" %JAVA_OPTS% -cp "%DIR%/vjtop.jar;%JAVA_HOME%/lib/tools.jar" com.vip.vjtools.vjtop.VJTop %*
