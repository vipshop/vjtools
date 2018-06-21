
#!/bin/bash

# change it here
LOGDIR="./logs"

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')


# Enable coredump
ulimit -c unlimited

## Memory Options##

MEM_OPTS="-Xms4g -Xmx4g -XX:NewRatio=1"

MEM_OPTS="$MEM_OPTS -XX:+AlwaysPreTouch"


if [[ "$JAVA_VERSION" < "1.8" ]]; then
  MEM_OPTS="$MEM_OPTS -XX:PermSize=256m -XX:MaxPermSize=512m"
else         
  MEM_OPTS="$MEM_OPTS -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"
fi


#MEM_OPTS="$MEM_OPTS -Xss256k"

#MEM_OPTS_="$MEM_OPTS -XX:ReservedCodeCacheSize=240M"


## GC Options ##

GC_OPTS="-XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:MaxTenuringThreshold=4 -XX:+UseCMSInitiatingOccupancyOnly"
GC_OPTS="$GC_OPTS -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+CMSParallelInitialMarkEnabled"
GC_OPTS="$GC_OPTS -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=1024"


#GC_OPTS="$GC_OPTS -XX:ParallelGCThreads=16 -XX:ConcGCThreads=8"

#if [[ "$JAVA_VERSION" > "1.8" ]]; then     
#  GC_OPTS="$GC_OPTS -XX:-CMSClassUnloadingEnabled"
#fi


## GC log Options ##

if [ -d /dev/shm/ ]; then
    GC_LOG_FILE=/dev/shm/gc-myapp.log
else
	GC_LOG_FILE=${LOGDIR}/gc-myapp.log
fi


if [ -f ${GC_LOG_FILE} ]; then
  GC_LOG_BACKUP =  ${LOGDIR}/gc-myapp-$(date +'%Y%m%d_%H%M%S').log
  echo "saving gc log ${GC_LOG_FILE} to ${GC_LOG_BACKUP}"
  mv ${GC_LOG_FILE} ${GC_LOG_BACKUP}
fi

GCLOG_OPTS="-Xloggc:${GC_LOG_FILE} -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCApplicationStoppedTime -XX:+PrintPromotionFailure"

if [[ "$JAVA_VERSION" < "1.8" ]]; then
	GCLOG_OPTS="$GCLOG_OPTS -XX:+PrintGCCause"
fi


## Performance Options##

PERFORMANCE_OPTS=" -XX:-UseBiasedLocking -XX:AutoBoxCacheMax=20000 -Djava.security.egd=file:/dev/./urandom"

#PERFORMANCE_OPTS="$PERFORMANCE_OPTS -XX:-UseCounterDecay"

#PERFORMANCE_OPTS="$PERFORMANCE_OPTS -XX:+PerfDisableSharedMem"

#if [[ "$JAVA_VERSION" > "1.8" ]]; then     
#  PERFORMANCE_OPTS="$PERFORMANCE_OPTS -XX:-TieredCompilation"
#fi


## Trouble shotting ##

SHOTTING_OPTS="-XX:+PrintCommandLineFlags -XX:-OmitStackTraceInFastThrow -XX:ErrorFile=${LOGDIR}/hs_err_%p.log"

#SHOTTING_OPTS="$SHOTTING_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGDIR}/"


## JMX ##

JMX_OPTS="-Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.port=7001 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"


## Other ##

OTHER_OPTS="-Djava.net.preferIPv4Stack=true"


## All together ##

JAVA_OTPS="$MEM_OPTS $GC_OPTS $GCLOG_OPTS $PERFORMANCE_OPTS $SHOTTING_OPTS $JMX_OPTS $OTHER_OPTS"

echo $JAVA_OTPS