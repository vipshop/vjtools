#!/bin/sh
# vjtop - java monitoring for the command-line 
# launch script
#
# author: Markus Kolb
# 
if [ -z "$JAVA_HOME" ] ; then
	    echo "JAVA_HOME env doesn't exist, try to find the location of java"
        JAVA_HOME=`readlink -f \`which java 2>/dev/null\` 2>/dev/null | \
        sed 's/\jre\/bin\/java//' | sed 's/\/bin\/java//'`
fi

TOOLSJAR="$JAVA_HOME/lib/tools.jar"

if [ ! -f "$TOOLSJAR" ] ; then
    echo "JAVA_HOME is $JAVA_HOME, $TOOLSJAR doesn't exist" >&2
    exit 1
fi

DIR=$( cd $(dirname $0) ; pwd -P )

"$JAVA_HOME"/bin/java -Xms256m -Xmx512m -XX:+UseSerialGC -XX:-TieredCompilation -XX:CICompilerCount=2 -XX:AutoBoxCacheMax=20000 -cp "$DIR/vjtop.jar:$TOOLSJAR" \
com.vip.vjtools.vjtop.VJTop "$@"
exit $?
