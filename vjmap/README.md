# 1. 概述

分代版的jmap（新生代，存活区，老生代），是排查内存缓慢泄露，老生代增长过快原因的利器。因为`jmap -histo PID` 打印的是整个Heap的对象信息，而很多时候，我们需要专门查看OldGen对象，以及survivor区Age较大的对象。

vjmap 原始思路来源于R大的[tbjmap](https://github.com/alibaba/TBJMap) ，翻新后兼容JDK8，支持对存活区老龄剩余对象的查询，能更直接发现问题。

注意：因为vjmap的原理，只支持CMS和ParallelGC，不支持G1.

# 2.使用说明

[Maven Central 下载](http://repo1.maven.org/maven2/com/vip/vjtools/vjmap/1.0.0/vjmap-1.0.0.zip) - 27k

注意：vjmap在执行过程中，会完全停止应用一段时间，必须摘流量执行！！！！

必须与目标JVM使用相同用户运行，如果执行时仍然有权限错误，改用root用户执行。

vjmap的运行需要一段时间，如果中途需要停止执行，请使用kill vjmap的进程号，让vjmap从目标进程退出。如果错用了kill -9 ，目标java进程会保持在阻塞状态不再工作，此时必须执行两次 kill -18 目标进程PID来重新唤醒目标java进程。

    
## 2.1 常用指令

```
// 打印整个堆中对象的统计信息，按对象的total size排序:
./vjmap.sh -all PID > /tmp/histo.log

// 打印老年代的对象统计信息，按对象的oldgen size排序，暂时只支持CMS:  
./vjmap.sh -old PID > /tmp/histo-old.log

// 打印Survivor区的对象统计信息，默认age>=3: 
./vjmap.sh -sur PID > /tmp/histo-sur.log

// 打印Survivor区的对象统计信息，age>=10
// 先增大晋升阈值-XX:MaxTenuringThreshold=xx, 通过查询age较高的对象，即必定会逃逸到cms区的对象。
./vjmap.sh -sur:minage=10 PID > /tmp/histo-sur.log
```

> 其中PID为目标java进程的进程号。



## 2.2 过滤对象大小，不显示过小的对象:

```
// 按对象的total size进行过滤，只打印占用超过1K的数据
./vjmap.sh -all:minsize=1024 PID > /tmp/histo.log

// 按对象的oldgen size进行过滤，只打印OldGen占用超过1K的数据
./vjmap.sh -old:minsize=1024 PID > /tmp/histo-old.log

// 按对象的survivor size进行过滤，只打印Survivor占用超过1K的数据
./vjmap.sh -sur:minsize=1024 PID > /tmp/histo-sur.log
```

## 2.3 按class name排序，配合大小过滤生成用于定时比较的报表:

```
./vjmap.sh -all:minsize=1024,byname PID > /tmp/histo.log

./vjmap.sh -old:minsize=1024,byname PID > /tmp/histo-old.log

./vjmap.sh -sur:minsize=1024,byname PID > /tmp/histo-sur.log
```

# 3.输出示例


## 3.1 Survivor区年龄大于N的对象统计

```
Survivor Object Histogram:

 #num  #count     #bytes #Class description
-----------------------------------------------------------------------------------
   1:      37         1k io.netty.buffer.PoolThreadCache$MemoryRegionCache$Entry
   2:       2         64 java.util.concurrent.locks.AbstractQueuedSynchronizer$Node
Total: 39/    1k over age 2

Heap traversal took 1.3 seconds.
```


# 4.修改记录

* 兼容JDK8
* 新功能：Survivor区 age大于N的对象统计
* 性能提升：直接访问Survivor或OldGen区，而不是用Heap Visitor回调的方式访问整个Heap
* 新配置项：按对象的大小进行过滤，不显示过小的对象
* 新配置项：按对象的名称进行排序，用于生成定时比较的报表
* 输出改进：报表数字的单位化(k,m,g)与对齐，OldGen报表默认按对象在OldGen的大小排序

```