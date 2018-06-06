#!/bin/sh

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME env is not set, try to find it out"

	JAVA_PATH=`which java 2>/dev/null`
	if [ "x$JAVA_PATH" != "x" ]; then
	  JAVA_PATH=`dirname $JAVA_PATH 2>/dev/null`
	  JAVA_HOME=`dirname $JAVA_PATH 2>/dev/null`
	fi
fi

SA_JDI_PATH=$JAVA_HOME/lib/sa-jdi.jar

echo -e "\033[31mWARNING!! STW(Stop-The-World) will be performed on your Java process, if this is NOT wanted, type 'Ctrl+C' to exit. \033[0m"

PRGDIR=`dirname "$0"`
BASEDIR=`cd "$PRGDIR/" >/dev/null; pwd`

if [ -f ${SA_JDI_PATH} ]; then
   java -XX:AutoBoxCacheMax=20000 -classpath $BASEDIR/vjmap.jar:$SA_JDI_PATH com.vip.vjtools.vjmap.VJMap $*
else
   echo "JAVA_HOME/lib/sa-jdi.jar is not exist, please set your JAVA_HOME env";
fi
