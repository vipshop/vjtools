#!/bin/sh

if [ -z "$JAVA_HOME" ] ; then
	echo "JAVA_HOME env doesn't exist, try to find the location of java"
    JAVA_HOME=`readlink -f \`which java 2>/dev/null\` 2>/dev/null | \
    sed 's/\jre\/bin\/java//' | sed 's/\/bin\/java//'`
fi

if [ ! -d "$JAVA_HOME" ] ; then
	echo "Please set JAVA_HOME env before run this script"
	exit 1
fi



# returns the JDK version.
# 8 for 1.8.0_nn, 9 for 9-ea etc, and "no_java" for undetected
GET_JDK_VERSION() {
  local result
  local java_cmd
  if [[ -n $(type -p java) ]]
  then
    java_cmd=java
  elif [[ (-n "$JAVA_HOME") && (-x "$JAVA_HOME/bin/java") ]]
  then
    java_cmd="$JAVA_HOME/bin/java"
  fi
  local IFS=$'\n'
  # remove \r for Cygwin
  local lines=$("$java_cmd" -Xms32M -Xmx32M -version 2>&1 | tr '\r' '\n')
  if [[ -z $java_cmd ]]
  then
    result=no_java
  else
    for line in $lines; do
      if [[ (-z $result) && ($line = *"version \""*) ]]
      then
        local ver=$(echo $line | sed -e 's/.*version "\(.*\)"\(.*\)/\1/; 1q')
        # on macOS, sed doesn't support '?'
        if [[ $ver = "1."* ]]
        then
          result=$(echo $ver | sed -e 's/1\.\([0-9]*\)\(.*\)/\1/; 1q')
        else
          result=$(echo $ver | sed -e 's/\([0-9]*\)\(.*\)/\1/; 1q')
        fi
      fi
    done
  fi
  echo "$result"
}

JDK_VERSION=$(GET_JDK_VERSION)
echo "JDK_VERSION : $JDK_VERSION"

# jdk 8 and before
if [[ $JDK_VERSION -le 8 ]]; then
    TOOLSJAR="$JAVA_HOME/lib/tools.jar"
    if [ ! -f "$TOOLSJAR" ] ; then
        echo "$TOOLSJAR doesn't exist" >&2
        exit 1
    fi
    JAVA_OPTS="-Xms256m -Xmx256m -XX:NewRatio=1 -Xss256k -XX:+UseSerialGC -XX:CICompilerCount=2 -Xverify:none -XX:AutoBoxCacheMax=20000"
else
    # jdk 9 or later
    JAVA_OPTS="-Xms256m -Xmx256m -XX:NewRatio=1 -Xss256k -XX:+UseSerialGC -XX:CICompilerCount=2 -XX:AutoBoxCacheMax=20000"
fi


DIR=$( cd $(dirname $0) ; pwd -P )

"$JAVA_HOME"/bin/java $JAVA_OPTS -cp "$DIR/vjmxcli.jar:$TOOLSJAR" com.vip.vjtools.jmx.Client $*