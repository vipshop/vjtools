#!/bin/sh

PRGDIR=`dirname "$0"`
BASEDIR=`cd "$PRGDIR/" >/dev/null; pwd`

if [ -z "$JAVA_HOME" ] ; then
        JAVA_HOME=`readlink -f \`which java 2>/dev/null\` 2>/dev/null | \
        sed 's/\/bin\/java//'`
fi

TOOLSJAR="$JAVA_HOME/lib/tools.jar"

if [ ! -f "$TOOLSJAR" ] ; then
        echo "$JAVA_HOME seems to be no JDK!" >&2
        exit 1
fi

JAVA_VERSION=$("$JAVA_HOME"/bin/java -version 2>&1 | awk -F '"' '/version/ {print $2}')

JAVA_OPTS="-Xms96m -Xmx96m -Xmn64m -Xss256k -XX:ReservedCodeCacheSize=2496k -XX:AutoBoxCacheMax=20000 -XX:+UseSerialGC -Djava.compiler=NONE -Xverify:none" 

"$JAVA_HOME"/bin/java $JAVA_OPTS -cp "$BASEDIR/vjmxcli.jar:$TOOLSJAR" \com.vip.vjtools.jmx.Client $*
