# 1. 概述

若你习惯以Top观察“OS指标及繁忙的进程”，也推荐以VJTop观看 “JVM进程指标 及 CPU最繁忙，内存占用最多的线程”。

JVM进程信息，一次拉取了JVM在操作系统层面和JVM层面的所有重要指标。

繁忙线程信息， 对比于“先top -H 列出线程，再执行一次jstack拿到全部线程，再手工换算threadId”的繁琐过程，既方便，又可以连续跟踪，更不会因为jstack造成JVM停顿。

因此，VJTop也常用于监控系统发现指标超阈值时，用钩子脚本调VJTop来纪录事发地的状况。

在[jvmtop](https://github.com/patric-r/jvmtop) 的基础上二次开发，结合 [SJK](https://github.com/aragozin/jvm-tools)的优点，从/proc ， PerfData，JMX等处，以更高的性能，获取更多的信息。

运行时不造成应用停顿，可在线上安全使用。


# 2. 使用说明


## 2.1 概述

[Maven Central 下载](http://repo1.maven.org/maven2/com/vip/vjtools/vjtop/1.0.2/vjtop-1.0.2.zip)

必须与目标JVM使用相同的JDK版本运行，必须与目标JVM使用相同用户运行。如果仍有问题，请看后面的执行问题排查章节。

```
// 占用CPU最多的线程
./vjtop.sh <PID> 
```

## 2.2 原理：

### 2.21 进程区数据来源

* 从/proc/PID/* 文件中获取进程数据
* 从JDK的PerfData文件中获取JVM数据(JDK每秒写入/tmp/hsperfdata_$userid/$pid文件的统计数据)
* 使用目标JVM的JMX中获取JVM数据（如果目标JVM还没启动JMX，通过attach方式动态加载）

如果数据同时在PerfData和JMX存在，优先使用PerfData，除非PerfData被屏蔽。 


### 2.2.2 线程区数据来源 

使用ThreadMxBean操作：

1. getAllThreadIds()获得所有Thread Id
2. getThreadCpuTime(tids)获得所有线程的CPU时间 (以及SYS CPU，内存分配)
3. 排序后，用getThreadInfo(tids)获得前10名线程的信息，因为不取线程的StackTrace，不会堵塞应用。


## 2.3 找出CPU最繁忙的线程


### 2.3.1 命令参数

```
// 按时间区间内，线程占用的CPU排序，默认显示前10的线程，默认每10秒打印一次
./vjtop.sh <PID>

// 按线程从启动以来的总占用CPU来排序
./vjtop.sh --totalcpu <PID>

// 按时间区间内，线程占用的SYS CPU排序
./vjtop.sh --syscpu <PID>

// 按线程从启动以来的总SYS CPU排序
./vjtop.sh --totalsyscpu <PID>
```

### 2.3.2 输出示例：

```
 PID: 57789 - 15:37:07, JVM: 1.8.0_144, USER: calvin, UPTIME: 01h07m
 PROCESS:  0.99% cpu ( 0.04% of 24 core), 2491m rss,   0m swap
 IO:   24k rchar,    1k wchar,    0 read_bytes,    0 write_bytes
 THREAD:   97 active,   89 daemon,   99 peak,  461 created, CLASS: 12243 loaded, 0 unloaded
 HEAP: 160m/819m eden, 0m/102m sur, 43m/1024m old
 NON-HEAP: 55m/256m cms perm gen, 8m/96m codeCache, 0m/0m direct, 0m/0m map
 GC: 0/0ms ygc, 0/0ms fgc, SAFE-POINT: 6 count, 1ms time, 1ms syncTime

    TID NAME                                                      STATE    CPU SYSCPU  TOTAL TOLSYS
     43 metrics-mercury-metric-logger-1-thread-1             TIMED_WAIT  0.38%  0.28% 25.48%  9.13%
    110 metrics-mercury-metric-logger-2-thread-1             TIMED_WAIT  0.38%  0.18% 25.43%  9.10%
    496 RMI TCP Connection(365)-192.168.200.87                 RUNNABLE  0.05%  0.05%  0.00%  0.00%
     82 Proxy-Worker-5-10                                      RUNNABLE  0.01%  0.01%  0.93%  0.30%
    120 threadDeathWatcher-6-1                               TIMED_WAIT  0.00%  0.00%  0.26%  0.09%
     98 Proxy-Worker-5-16                                      RUNNABLE  0.00%  0.00%  0.80%  0.26%
     99 Proxy-Worker-5-17                                      RUNNABLE  0.00%  0.00%  0.92%  0.31%
     63 Proxy-Worker-5-2                                       RUNNABLE  0.00%  0.00%  1.07%  0.37%
     70 Proxy-Worker-5-5                                       RUNNABLE  0.00%  0.00%  0.78%  0.26%
    102 Proxy-Worker-5-20                                      RUNNABLE  0.00%  0.00%  0.80%  0.27%

 Total cpu:  1.01% (user= 0.31%, sys= 0.70%), top 10 threads are shown, order by CPU
 Cost time:  46ms, CPU time:  60ms
```
进程区数据解释:

* `rss`: `Resident Set Size`, 该进程在内存中的页的数量。该数据从/proc/\<pid\>/status中获取, 含义与[proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)中一致。
* `swap`: 被交换出去的虚存大小。该数据从/proc/\<pid\>/status中获取, 含义与[proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)中一致。
* `rchar/wchar`: 通过系统调用的读/写的字节数。包含从PageCache的读写，该数据从/proc/\<pid\>/io中获取，含义与[proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)中一致。
* `read_bytes/write_bytes`: 真正达到存储层的读/写的字节数。该数据从/proc/\<pid\>/io中获取，含义与[proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)中一致。
* `codeCache`: JIT编译的二进制代码的存放区，满后将不能编译新的代码。
* `direct`: 堆外内存，但注意新版Netty不经过JDK API所分配的堆外内存未能纪录。
* `SAFE-POINT`: PerfData开启时可用，JVM真正的停顿次数及停顿时间。


线程区数据解释:

* `CPU`: 线程在打印间隔内所占的CPU百分比(按单个核计算)
* `SYSCPU`: 线程在打印间隔内所占的SYS CPU百分比(按单个核计算)
* `TOTAL`: 从进程启动到现在，线程的总CPU时间/进程的总CPU时间的百分比
* `TOLSYS`: 从进程启动到现在，线程的总SYS CPU时间/进程的总CPU时间的百分比

底部数据解释:

* `Cost time`: 本次采集数据及输出的耗时
* `CPU time`: 本次采集数据及输出的CPU时间占用

## 2.4 找出内存分配最频繁的线程


### 2.4.1 命令参数

```
// 线程分配内存的速度排序，默认显示前10的线程，默认每10秒打印一次
./vjtop.sh --memory <PID>

// 按线程的总内存分配而不是打印间隔内的内存分配来排序
./vjtop.sh --totalmemory <PID>
```

### 2.4.2 输出示例

```
(忽略头信息)
 THREADS-MEMORY:   30k/s allocation rate

    TID NAME                                                 STATE         MEMORY         TOTAL-ALLOCATED
  47636 RMI TCP Connection(583)-127.0.0.1                 RUNNABLE   27k/s(88.76%)    17m( 0.00%)
      1 main                                              RUNNABLE    2k/s( 8.44%)   370g(83.16%)
  47845 JMX server connection timeout 47845             TIMED_WAIT   251/s( 0.80%)    21k( 0.00%)
  46607 Worker-501                                      TIMED_WAIT    60/s( 0.19%)   934m( 0.20%)
  46609 Worker-502                                      TIMED_WAIT    60/s( 0.19%)   822m( 0.18%)
  46610 Worker-503                                      TIMED_WAIT    60/s( 0.19%)   737m( 0.16%)
  46763 Worker-504                                      TIMED_WAIT    60/s( 0.19%)   696m( 0.15%)
  46764 Worker-505                                      TIMED_WAIT    60/s( 0.19%)   743m( 0.16%)
  47149 Worker-506                                      TIMED_WAIT    60/s( 0.19%)   288m( 0.06%)
  46551 Worker-500                                      TIMED_WAIT    60/s( 0.19%)   757m( 0.17%)
```
进程区数据解释:
* `allocation rate`: 所有线程在打印间隔内每秒分配的内存

线程区数据解释:

* `STATE`: 该线程当前的状态
* `MEMORY`: 该线程分配内存的瞬时值，即该线程在打印间隔内每秒分配的内存空间(该线程每秒分配的内存占所有线程在该秒分配的总内存的百分比)
* `TOTAL-ALLOCATED`: 该线程分配内存的历史累计值，即从进程启动到现在，该线程分配的总内存大小，该总内存大小包括已回收的对象的内存(该线程分配的总内存大小占所有线程分配的总内存大小的百分比)。


## 2.5 实时交互

### 2.5.1 打印线程Stack Trace

在页面中输入t，再选择线程号，可打印线程的Stack Trace，看繁忙的线程在忙什么。

```
 Cost time:  55ms, CPU time:  68ms
 Input command (h for help):t
 Input TID for stack:4161
	at java.lang.Object.wait(Native Method)
	at org.eclipse.core.internal.jobs.WorkerPool.sleep(WorkerPool.java:188)
	at org.eclipse.core.internal.jobs.WorkerPool.startJob(WorkerPool.java:220)
	at org.eclipse.core.internal.jobs.Worker.run(Worker.java:52)
```

### 2.5.2 实时切换显示模式

在页面中输入m，可选择模式显示和排序模式。 
```
 Input command (h for help):m
 Input number of Display Mode(1.cpu, 2.syscpu 3.total cpu 4.total syscpu 5.memory 6.total memory): 5
 Display mode changed to memory for next flush
```

其他选项包括l：显示的线程数，i：刷新的频率

```
 Input command (h for help):l
 Input number of threads to display :20
 Number of threads to display changed to 20 for next flush
```

## 2.6 公共参数

```
// 打印其他选项
./vjtop.sh -h

// 结果输出到文件
./vjtop.sh <PID> > /tmp/vjtop.log

// 每5秒打印一次（默认10秒）
./vjtop.sh -d 5 <PID>

// 显示前20的线程（默认10）
./vjtop.sh -l 20 <PID>

// 更宽的120字节的屏幕 （默认100）
./vjtop.sh -w 120 <PID> > /tmp/vjtop.log

// 打印20次后退出
./vjtop.sh -n 20 <PID>
```

# 3. 执行问题排查

首先，运行vjtop的JDK，与目标JDK的版本必须一致

其次，vjtop 使用JVM attach机制 连入PID 并获得JMX的本地连接地址，如果出现如下出错，可能的原因有

```
ERROR: Could not attach to process.
```


1. 执行vjtop的用户，对/tmp/.java_pid$PID 文件有读写权限，该文件权限为srw------- 1，所以需要相同用户

2. /tmp/.java_pid$PID 文件在首次连接时会生成，但如果生成之后被/tmp 目录的清理程序错误删除，JVM将不再能连入，只能重启应用。

3. 目标JVM使用启动参数-Djava.io.tmpdir，重定向了tmp目录路径

4. 目标JVM使用启动参数-XX:+DisableAttachMechanism禁止了attach

如果实在没有办法attach，可以考虑在原目标进程中配置JMX启动参数，设定JMX的地址与端口，然后在vjtop中指定

目标进程的JVM参数：
```
-Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.port=7001 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.X=false -Dcom.sun.management.jmxremote.ssl=false
```

vjtop的命令(since 1.0.3):

```
./vjtop.sh -j 127.0.0.1:7001 <PID>
```


# 4. 改进点

### 4.1 进程概览

* 新功能：进程的物理内存，SWAP，IO信息
* 新功能：将内存信息与GC信息拆开不同分代独立显示
* 新功能：显示CodeCache与堆外内存信息


### 4.2 热点线程

* 新功能：线程内存分配速度的展示与排序 (from SJK)
* 新功能：线程SYS CPU的展示与排序，应用启动以来线程的总CPU间的排序 (from SJK)
* 新配置项：打印间隔，展示线程数
* 性能优化：减少了几倍的耗时，通过批量获取线程CPU时间(from SJK)等方法

### 4.3 实时交互(since 1.0.1)

* 新功能： 选择打印某条线程的stack trace
* 新功能： 打印全部的线程名
* 新功能： 实时切换显示模式和排序，刷新频率和显示线程数

### 4.4 为在生产环境运行优化：

* 删除jvmtop会造成应用停顿的Profile页面
* 删除jvmtop获取所有Java进程信息，有着不确定性的Overview页面
* 默认打印间隔调整到10s
* 显示vjtop自身的消耗