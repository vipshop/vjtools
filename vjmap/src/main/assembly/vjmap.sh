#!/bin/sh

if [ -z "$JAVA_HOME" ] ; then
	    echo "JAVA_HOME env doesn't exist, try to find the location of java"
        JAVA_HOME=`readlink -f \`which java 2>/dev/null\` 2>/dev/null | \
        sed 's/\jre\/bin\/java//' | sed 's/\/bin\/java//'`
fi

SAJDI_PATH=$JAVA_HOME/lib/sa-jdi.jar

if [ ! -f "$SAJDI_PATH" ] ; then
    echo "JAVA_HOME is $JAVA_HOME, $SAJDI_PATH doesn't exist !" >&2
    exit 1
fi

DIR=$( cd $(dirname $0) ; pwd -P )
JAVA_OPTS="-Xms256m -Xmx256m -XX:NewRatio=1 -Xss256k -XX:ReservedCodeCacheSize=72M -XX:+UseSerialGC -Xverify:none -XX:AutoBoxCacheMax=20000"

echo -e "\033[31mWARNING!! STW(Stop-The-World) will be performed on your Java process, if this is NOT wanted, type 'Ctrl+C' to exit. \033[0m"


java $JAVA_OPTS -classpath $DIR/vjmap.jar:$SAJDI_PATH com.vip.vjtools.vjmap.VJMap $*