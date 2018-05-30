# 1. vjmap概述

`jmap -histo PID` 打印的是整个Heap的对象信息，而很多时候，我们需要专门查看OldGen对象，survivor区Age较大的对象，以找出内存缓慢泄漏，或者老生代增长过快的原因。

vjmap 原始思路来源于[tbjmap](https://github.com/alibaba/TBJMap) 进行了较大的二次开发，如兼容JDK8，查找Survivor区年龄大于N的对象等。

注意：因为vjmap的原理，只支持CMS，而不支持G1.

# 2.vjmap使用

maven编译后得到zip包，解压后运行。

注意：vjmap在执行过程中，会完全停止应用，必须摘流量执行！！！

必须与目标JVM使用相同用户运行，如果执行时仍然有权限错误，改用root用户执行。

    
* 常用指令

```
// 打印新老生代对象的统计信息，按对象的total size排序, 支持CMS 和 ParallelGC:
./vjmap.sh -all PID > /tmp/histo.log

// 打印老年代的对象统计信息，按对象的oldgen size排序，暂时只支持CMS:  
./vjmap.sh -old PID > /tmp/histo-old.log

// 打印Survivor区的对象统计信息，默认age>=3，支持CMS 和 ParallelGC: 
./vjmap.sh -sur PID > /tmp/histo-sur.log

// 打印Survivor区的对象统计信息，age>=6
// 先增大晋升阈值-XX:MaxTenuringThreshold=xx, 通过查询age较高的对象，即必定会逃逸到cms区的对象。
./vjmap.sh -sur:minage=6 PID > /tmp/histo-sur.log
```

> 其中PID为目标java进程的进程号。

> 生产上运行请先摘除流量 !!!!!

> 如果kill -9 vjmap的进程，没有正常退出vjmap， 目标java进程会卡住（kill vjmap 无此问题），执行两次 kill -18 PID来唤醒目标java进程

* 过滤对象大小，不显示过小的对象:

```
// 按对象的total size进行过滤，只打印占用超过1K的数据
./vjmap.sh -all:minsize=1024 PID > /tmp/histo.log

// 按对象的oldgen size进行过滤，只打印OldGen占用超过1K的数据
./vjmap.sh -old:minsize=1024 PID > /tmp/histo-old.log

// 按对象的survivor size进行过滤，只打印Survivor占用超过1K的数据
./vjmap.sh -sur:minsize=1024 PID > /tmp/histo-sur.log
```

* 按class name排序，配合大小过滤生成用于定时比较的报表:

```
./vjmap.sh -all:minsize=1024,byname PID > /tmp/histo.log

./vjmap.sh -old:minsize=1024,byname PID > /tmp/histo-old.log

./vjmap.sh -sur:minsize=1024,byname PID > /tmp/histo-sur.log
```

# 3.输出示例


* Survivor区年龄大于N的对象统计

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
* 新配置项：按对象的总大小进行过滤，不显示过小的对象
* 新配置项：按对象的名称进行排序，用于生成定时比较的报表
* 输出改进：报表数字的单位化(k,m,g)与对齐，OldGen报表默认按对象在OldGen的大小排序

```