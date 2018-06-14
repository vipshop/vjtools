# 1. 启用JMX

```
-Dcom.sun.management.jmxremote.port=7001 -Dcom.sun.management.jmxremote 
-Dcom.sun.management.jmxremote.authenticate=false 
-Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1
```


# 2. 常用JMX项

| 子项目 | Object Name | Attribute Name|
| -------- | -------- | -------- |
| 堆内存    |  java.lang:type=Memory | HeapMemoryUsage     |
| 非堆内存(不包含堆外内存)    |  java.lang:type=Memory | NonHeapMemoryUsage     |
| 堆外内存(不包含新版Netty申请的堆外内存)    |  java.nio:type=BufferPool,name=direct |MemoryUsed |
| 线程数    |  java.lang:type=Threading | ThreadCount |
| 守护线程数    |  java.lang:type=Threading | DaemonThreadCount |
| 分代内存及GC  |  不同JDK的值不一样TODO | 不同JDK的值不一样TODO  |


# 3. vjmxcli 

## 3.1 概述

定位：在cmdline-jmxclient项目上定制，增加功能

* 支持以pid接入JVM，且不需要预先接入
* 完全模拟`jstat -gcutil`输出的`gcutil`
* 用于falcon，一次返回GCUtil信息加上线程信息的`all`

性能：每调度一次`java -jar vjmxclient.jar`，其实是创建了一个新的JVM，因此在vjmxcli.sh 加上了一系列JVM参数减少消耗。

## 3.2 获取MBean属性值

```
// 以host:port接入
./vjmxcli.sh - 127.0.0.1:8060 java.lang:type=Memory HeapMemoryUsage

// 以pid接入
./vjmxcli.sh - 98583 java.lang:type=Memory HeapMemoryUsage
```

参数解释

* `-` : 无密码
* `127.0.0.1:8060` or `98582` : 应用地址及jmx端口, 或pid
* `java.lang:type=Memory`:MBean名
* `java.lang:type=Memory`:Attribute名


## 3.3 GCutil

```
//一次性输出
./vjmxcli.sh - 127.0.0.1:8060 gcutil

//间隔5秒连续输出
./vjmxcli.sh - 127.0.0.1:8060 gcutil 5

```
JDK7 示例输出

```
S	      S	      E	     O	     P	   	YGC	YGCT	FGC	FGCT	GCT	
41.25	41.25	2.25	0.00	0.48	2	0.025	0	0.0	   0.025
```

JDK8 示例输出
```
S	      S	      E	     O	      M	   CCS	YGC	YGCT	FGC	FGCT	GCT	
41.25	41.25	2.25	0.00	0.48	0	2	0.025	0	0.0	   0.025
```
