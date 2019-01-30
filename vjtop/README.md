# 1. 概述

对应于观看“OS指标及繁忙进程”的top，vjtop就是观察“JVM进程指标及其繁忙线程”的首选工具。

**JVM进程信息**：收集了进程在OS层面和JVM层面的所有重要指标。大家为什么喜欢用[dstat](http://dag.wiee.rs/home-made/dstat/)看OS状态，因为它将你想看的数据全都收集呈现眼前了，vjtop也是这样的风格。

**繁忙线程信息**： 对比于“先top -H 列出线程，再执行jstack拿到全部线程，再手工换算十与十六进制的线程号”的繁琐过程，vjtop既方便，又可以连续跟踪，更不会因为jstack造成JVM停顿。

对于超出正常范围的值，vjtop还很贴心的进行了变色显示。

运行时不造成应用停顿，可在线上安全使用。

**常用场景：**

1. 性能问题快速定位，用vjtop显示出CPU繁忙或内存消耗大的线程，再实时交互翻查该线程的statk trace。

2. 压测场景，用vjtop实时反馈JVM进程状态，类似于用dstast对操作系统指标的监控。

3. 生产环境，当应用出现问题时，用vjtop快速了解进程的状态。还可与监控系统结合，发现指标(如CPU、超时请求数)超出阈值时，用钩子脚本调用vjtop来纪录事发地的状况。


在[jvmtop](https://github.com/patric-r/jvmtop) 的基础上二次开发，结合 [SJK](https://github.com/aragozin/jvm-tools)的优点，从/proc ， PerfData，JMX等处，以更高的性能，获取更多的信息。


# 2. 使用说明


## 2.1 概述

[Download 1.0.8.zip](http://repo1.maven.org/maven2/com/vip/vjtools/vjtop/1.0.8/vjtop-1.0.8.zip) (from Maven Central)

vjtop运行所需权限与jstack相同，必须与目标JVM使用相同的JDK版本运行，必须与目标JVM使用相同用户运行。如果仍有问题，请看后面的执行问题排查章节。

```
// 占用CPU最多的线程
./vjtop.sh <PID> 

// 打印选项，每个版本的参数会有变动，特别是模式参数，以help信息为准
./vjtop.sh -h

```

## 2.2 找出CPU最繁忙的线程


### 2.2.1 命令参数

```
// 按时间区间内，线程占用的CPU排序，默认显示前10的线程，默认每10秒打印一次
./vjtop.sh <PID>

// 按时间区间内，线程占用的SYS CPU排序
./vjtop.sh -m syscpu <PID>

// 按线程从启动以来的总占用CPU来排序
./vjtop.sh -m totalcpu <PID>

// 按线程从启动以来的总SYS CPU排序
./vjtop.sh -m totalsyscpu <PID>
```


### 2.2.2 输出示例：

```
 PID: 191082 - 17:43:12 JVM: 1.7.0_79 USER: calvin UPTIME: 2d02h
 PROCESS: 685.00% cpu(28.54% of 24 core), 787 thread
 MEMORY: 6626m rss, 6711m peak, 0m swap | DISK: 0B read, 13mB write
 THREAD: 756 live, 749 daemon, 1212 peak, 0 new | CLASS: 15176 loaded, 161 unloaded, 0 new
 HEAP: 630m/1638m eden, 5m/204m sur, 339m/2048m old
 NON-HEAP: 80m/256m/512m perm, 13m/13m/240m codeCache
 OFF-HEAP: 0m/0m direct(max=2048m), 0m/0m map(count=0), 756m threadStack
 GC: 6/66ms/11ms ygc, 0/0ms fgc | SAFE-POINT: 6 count, 66ms time, 5ms syncTime


    TID NAME                                                      STATE    CPU SYSCPU  TOTAL TOLSYS
     23 AsyncAppender-Worker-ACCESSFILE-ASYNC                   WAITING 23.56%  6.68%  2.73%  0.72%
    560 OSP-Server-Worker-4-5                                  RUNNABLE 22.58% 10.67%  1.08%  0.48%
   9218 OSP-Server-Worker-4-14                                 RUNNABLE 22.37% 11.45%  0.84%  0.40%
   8290 OSP-Server-Worker-4-10                                 RUNNABLE 22.36% 11.24%  0.88%  0.41%
   8425 OSP-Server-Worker-4-12                                 RUNNABLE 22.24% 10.72%  0.98%  0.47%
   8132 OSP-Server-Worker-4-9                                  RUNNABLE 22.00% 10.68%  0.90%  0.42%
   8291 OSP-Server-Worker-4-11                                 RUNNABLE 21.80% 10.09%  0.89%  0.41%
   8131 OSP-Server-Worker-4-8                                  RUNNABLE 21.68%  9.77%  0.93%  0.44%
   9219 OSP-Server-Worker-4-15                                 RUNNABLE 21.56% 10.43%  0.90%  0.41%
   8426 OSP-Server-Worker-4-13                                 RUNNABLE 21.35% 10.42%  0.66%  0.31%

 Total  : 668.56% cpu(user=473.25%, sys=195.31%) by 526 atcive threads(which cpu>0.05%)
 Setting: top 10 threads order by CPU, flush every 10s
 Input command (h for help):
```
进程区数据解释:

* `PROCESS`: `thread`: 进程的操作系统线程数, `cxtsw`为主动与被动的线程上下文切换数
* `MEMORY`: `rss` 为 Resident Set Size, 进程实际占用的物理内存; `peak`为最峰值的rss; `swap`为进程被交换到磁盘的虚拟内存。
* `DISK`: 真正达到物理存储层的读/写的速度。
* `THREAD`: Java线程数, `active`为当前线程数, `daemon`为active线程中的daemon线程数, `new`为刷新周期内新创建的线程数。
* `CLASS`: `loaded`为当前加载的类数量，`unloaded`为总卸载掉的类数量，`new`为刷新周期内新加载的类数量。
* `HEAP`: 1.0.3版开始每一项有三个数字, 分别为1.当前使用内存, 2.当前已申请内存, 3.最大内存; 如果后两个数字相同时则合并。
* `sur`: 当前存活区的大小，注意实际有from, to 两个存活区。
* `NON-HEAP`: 数字含义同`HEAP`
* `codeCache`: JIT编译的二进制代码的存放区，满后将不能编译新的代码。
* `direct`: 堆外内存，三个数字含义同`HEAP`, 未显式设置最大内存时，约等于堆内存大小。注意新版Netty不经过JDK API所分配的堆外内存未在此统计。
* `map`: 映射文件内存，三个数字分别为1. map数量，2.当前使用内存，3.当前已申请内存，没有最大值数据。
* `threadStack`: Java线程所占的栈内存总和，但不包含VM线程。(since 1.0.3)
* `ygc`: YoungGC, 三个数字分别为次数／总停顿时间／平均停顿时间
* `fgc`: OldGC ＋ FullGC， 两个数字分别为次数／总执行时间，注意此时间仅为执行时间，非JVM停顿时间。
* `SAFE-POINT`: PerfData开启时可用，JVM真正的停顿次数及停顿时间，以及等待所有线程进入安全点所消耗的时间。


线程区数据解释:

* `CPU`: 线程在打印间隔内使用的CPU百分比(按单个核计算)
* `SYSCPU`: 线程在打印间隔内使用的SYS CPU百分比(按单个核计算)
* `TOTAL`: 从进程启动到现在，线程的总CPU时间/进程的总CPU时间的百分比
* `TOLSYS`: 从进程启动到现在，线程的总SYS CPU时间/进程的总CPU时间的百分比

底部数据解释:

* 如果该线程的平均使用CPU少于单核的0.1%，这条线程将不参与排序显示，减少消耗。 


## 2.3 找出内存分配最频繁的线程


### 2.3.1 命令参数

```
// 线程分配内存的速度排序，默认显示前10的线程，默认每10秒打印一次
./vjtop.sh -m 5 <PID>

// 按线程的总内存分配而不是打印间隔内的内存分配来排序
./vjtop.sh -m 6 <PID>
```

### 2.3.2 输出示例

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

如果该线程的平均内存分配速度少于1K/s，这条线程将不参与排序显示，减少消耗。 


## 2.4 命令行参数

```
// 打印其他选项
./vjtop.sh -h

// 结果输出到文件
./vjtop.sh <PID> > /tmp/vjtop.log

// 每5秒打印一次（默认10秒）
./vjtop.sh -i 5 <PID>

// 打印20次后退出
./vjtop.sh -n 20 <PID>

// 显示前100的线程（默认10）
./vjtop.sh -l 100 <PID>

// 不带变色与换页控制码的console模式，适合不支持控制码的终端。在Windows及输出到文件时将默认使用次此模式
./vjtop.sh -o clean <PID>

// key:value式的文本模式，适用于第三方工具采集vjtop的输出结果
./vjtop.sh -o text <PID>

// 只采集JVM信息，不采集繁忙线程信息
./vjtop.sh -c jvm <PID>

// 只采集繁忙线程信息，不采集JVM信息
./vjtop.sh -c thread <PID>

// 只显示线程名包含worker字样的线程，在热点线程与实时交互打印线程时都会过滤（1.0.6版开始忽略大小写）
./vjtop.sh -f worker <PID>

// 更宽的120字节的屏幕 （默认100）
./vjtop.sh -w 120 <PID> > /tmp/vjtop.log
```


## 2.5 实时交互

### 2.5.1 打印线程Stack Trace

1.在页面中输入t，再输入线程号，可打印线程的Stack Trace，看繁忙的线程在忙什么。

会引入暂停，但只取一条线程信息时停顿非常短

```
 Input command (h for help):s
 Input TID for stack:4161
	at java.lang.Object.wait(Native Method)
	at org.eclipse.core.internal.jobs.WorkerPool.sleep(WorkerPool.java:188)
	at org.eclipse.core.internal.jobs.WorkerPool.startJob(WorkerPool.java:220)
	at org.eclipse.core.internal.jobs.Worker.run(Worker.java:52)
```

上例子也可以直接输入 "s 4161"

2. 打印Top繁忙线程的stack trace

会引入暂停，但只取若干条线程信息时停顿非常短

```
 Input command (h for help):t
 Stack trace of top 10 threads:
 15: "RMI TCP Connection(15)-10.100.150.221"
   java.lang.Thread.State: RUNNABLE
	at sun.management.ThreadImpl.getThreadInfo1(Native Method)
	at sun.management.ThreadImpl.getThreadInfo(ThreadImpl.java:178)
	...
```

3. 打印所有状态为BLOCKED的线程的stack trace

会引入暂停，取所有线程信息，时间比前面两个命令略长。但因为不获取锁信息，也不拿JNI global references等数据，所以仍比jstack快。

```
 Input command (h for help):b

 Stack trace of blocked threads:
 ...

```

4. 打印所有线程的TID和线程名

不引入暂停

```
Thread Id and name of all live threads:
 16	: "JMX server connection timeout 16" (TIMED_WAITING)
 15	: "RMI TCP Connection(15)-10.100.150.221" (RUNNABLE)
 13	: "RMI Scheduler(0)" (TIMED_WAITING)
 11	: "RMI TCP Accept-0" (RUNNABLE)
 9	: "Attach Listener" (RUNNABLE)
 4	: "Signal Dispatcher" (RUNNABLE)
```

### 2.5.2 实时切换显示模式

1.改变显示和排序模式，在页面中输入m
```
 Input command (h for help):m
 Input number of Display Mode(1.cpu, 2.syscpu 3.total cpu 4.total syscpu 5.memory 6.total memory, current cpu): 5
```

2.改变显示间隔
``` 
 Input command (h for help):i
 Input flush interval seconds(current 10):20
 Flush interval change to 20 seconds
```
也可以直接输入"i 20" 切换 


3. 设定显示的线程数 

```
 Input command (h for help):l
 Input number of threads to display :20
 Number of threads to display changed to 20 for next flush
```
也可以直接输入"l 20" 切换 

4.设定按线程名过滤线程，在打印繁忙线程和全部线程时，线程名都必须包含filter字符串，大小写不敏感，不支持正则匹配和匹配符匹配。

```
 Input command (h for help):f
 Input filter of thread name (current null):Worker
 thread name filter change to "Worker" for next flush (3s later)
```



# 3. 原理

## 3.1 进程区数据来源

* 从/proc/PID/* 文件中获取进程数据, 详见[proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)
* 从JDK的PerfData文件中获取JVM数据(JDK每秒写入/tmp/hsperfdata_$userid/$pid文件的统计数据)
* 使用目标JVM的JMX中获取JVM数据（如果目标JVM还没启动JMX，通过attach方式动态加载）

如果数据同时在PerfData和JMX存在，优先使用PerfData。 
网络流量数据在/proc/PID/*中未能按进程区分，因此不再监控。

## 3.2 线程区数据来源 

使用ThreadMxBean操作：

1. getAllThreadIds()获得所有Thread Id
2. getThreadCpuTime(tids)获得所有线程的CPU时间 (以及SYS CPU，内存分配)
3. 排序后，用getThreadInfo(tids)获得前10名线程的信息，因为不取线程的StackTrace，不会堵塞应用。

# 4. 监控值变色告警规则

* 进程CPU：服务器总CPU50%黄， 70%红
* 进程线程：如果服务器少于8核，线程数为核数＊150 黄，核数＊225 红。 如果大于8核，核数＊100 黄, 核数＊150红。
* 进程内存： swap一旦使用即为红
* 进程磁盘IO: 读写磁盘30MB/s 黄，100MB/s 红
* CLASS: 新加载类时为黄色, 当前加载类8万为黄，15万为红
* JVM内存: 老生代，永久代，CodeCache，如果设置了Max，则Max的85%黄，95%红
* JVM线程: active规则同进程线程，new规则：新建1条线程为黄，每秒创建2条为红。
* YGC: YGC次数每秒1次以上黄，2次以上红，平均耗时100ms黄，200ms红, 总耗时达到了应用运行时间的5%黄，10%红
* FGC: 周期内的次数：1次黄，2次红
* SAFEPOINT: 安全点次数每秒2次黄，4次红，总耗时达到了应用运行时间的5%黄，10%红



# 5. 执行问题排查

1. JDK版本错误或tools.jar不存在

vjtop使用的java为JAVA_HOME/bin/java, 需要JDK7及以上，但"不要求"与目标应用的JVM使用相同的JDK版本。

vjtop需要依赖JAVA_HOME/lib/tools.jar

JAVA_HOME的定位，通过读取环境变量JAVA_HOME，如果没有定义，则尝试通过"which java"定位java从而获得相对路径。

2. 不能连入目标jvm

再次，vjtop 使用JVM attach机制 连入PID 并获得JMX的本地连接地址，所需权限与jstack相同， attach失败时出现如下报错

```
ERROR: Could not attach to process.
```

可以先执行jstack PID对比一下效果。

可能的原因有：

1. PID写错，进程不存在

2. VM Attach时，会强制检查执行vjtop的用户，与目标JMV的用户一致，否则会抛出"well-known file is not secure"之类的异常。

如果用户有sudo权限，可以尝试切换到目标用户，并把JAVA_HOME等环境变量带到新用户。
```
sudo -E su - <targetUser>
```

3. /tmp/.java_pid$PID 文件在首次连接时会生成，但如果生成之后被大家的文件清理脚本错误删除，JVM将不再能连入，只能重启应用。

4. 目标JVM使用启动参数-Djava.io.tmpdir，重定向了tmp目录路径，导致读不到/tmp/.java_pid$PID 文件。

5. 目标JVM使用启动参数-XX:+DisableAttachMechanism禁止了attach。

如果实在没有办法attach，可以考虑在原目标进程中配置JMX启动参数，设定JMX的地址与端口，然后在vjtop中指定该地址

目标进程的JVM参数：
```
-Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.port=7001 -Dcom.sun.management.jmxremote
 -Dcom.sun.management.jmxremote.X=false -Dcom.sun.management.jmxremote.ssl=false
```

vjtop的命令(since 1.0.3):

```
./vjtop.sh -j 127.0.0.1:7001 <PID>
```


# 6. 与jvmtop相比的改进点

### 6.1 进程概览

* 新功能：进程的物理内存，SWAP，IO，物理线程信息。
* 新功能：将内存信息与GC信息拆开不同分代独立显示
* 新功能：CodeCache与堆外内存，Thread Stack内存信息，SafePoint等信息
* 新功能：对偏离正常范围的值，进行变色提示


### 6.2 热点线程

* 新功能：线程内存分配速度的展示与排序 (from SJK)
* 新功能：线程SYS CPU的展示与排序，应用启动以来线程的总CPU间的排序 (from SJK)
* 新配置项：打印间隔，展示线程数

### 6.3 实时交互

* 新功能： 选择打印某条线程的线程栈，所有TopN繁忙线程的栈，所有Blocked状态线程的栈
* 新功能： 打印全部的线程名
* 新功能： 实时切换显示模式和排序，刷新频率和显示线程数

### 6.4 为在生产环境运行优化

* 删除jvmtop会造成应用停顿的Profile页面
* 删除jvmtop获取所有Java进程信息，有着不确定性的Overview页面
* 默认打印间隔调整到10s
* 进程信息尝试从PerfData而不是JMX读取数据，减少消耗
* 线程信息减少了几倍的耗时，通过批量获取线程CPU时间(from SJK)等方法
* 支持输出文本格式给第三方监控工具使用
* 支持只输出JVM信息或繁忙线程信息
* 支持vm attach总是失败时，直接配置JMX的方式连入
