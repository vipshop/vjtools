# VJMap

VJMap prints per GC generation (Eden, Survivor, OldGen) object details of a given process , it is an advanced way to find the reasons of memory leak and fast-growing OldGen.

# 1. Introduction

Jmap can display whole shared object memory maps or whole heap memory details ,but sometimes you may prefer to know the OldGen object counting and survivor object age counting，VJMap will list such information for you.

Initially inspired by [tbjmap](https://github.com/alibaba/TBJMap), JDK8 compatibility was added as well as query on aged 
survivor objects.

**[Note]**: G1 is unsupported.  Use it with CMS and ParallelGC only.

# 2. Getting Started

[download vjmap-1.0.4.zip](http://repo1.maven.org/maven2/com/vip/vjtools/vjmap/1.0.4/vjmap-1.0.4.zip)(from Maven Central)

**[Important]**: VJMap DOES cause stop-of-the-world of the target app. Make sure the target app is isolated from user 
access before you start using VJMap in production.

Run VJMap under **the same user who started the target process**. If access errors are still met, try again with 
root user.

VJMap may take quite some time to finish. Use `kill <PID_OF_VJMap>` to allow for a graceful exit. If `kill -9 <PID_OF_VJMap>` 
is mistakenly issued to the VJMap process, the target app will end up in blocked state, in which case you will have to 
execute `kill -18 <PID_OF_TARGET_APP>` TWICE to awaken the target app.

## 2.1 Commands

```
// Prints object stats of all gens, ordered by their respective size in total.
./vjmap.sh -all PID > /tmp/histo.log

// Prints oldgen object stats, ordered by size in OldGen. Only CMS is supported for this option. 
./vjmap.sh -old PID > /tmp/histo-old.log


// Prints survivor objects over the age of 3.
./vjmap.sh -sur PID > /tmp/histo-sur.log


// Prints survivor objects over the age of 10, as desinated by the argument -sur:minage=10
// When the promotion threshold -XX:MaxTenuringThreshold is lifted, objects with a high age value will be bound 
for the CMS oldgen
./vjmap.sh -sur:minage=10 PID > /tmp/histo-sur.log
```

> PID is the process ID of target java application

## 2.2 Display Larger Objects, Leaving Smaller Ones Out

```
// Shows objects with sizes over 1KB over the whole heap
./vjmap.sh -all:minsize=1024 PID > /tmp/histo.log

// shows objects with sizes over 1KB in OldGen specifically 
./vjmap.sh -old:minsize=1024 PID > /tmp/histo-old.log

// shows objects with sizes over 1KB in survivor space 
./vjmap.sh -sur:minsize=1024 PID > /tmp/histo-sur.log
```

## 2.3 Order by Classname and Filter by Size for Periodic Comparisons

```
./vjmap.sh -all:minsize=1024,byname PID > /tmp/histo.log

./vjmap.sh -old:minsize=1024,byname PID > /tmp/histo-old.log

./vjmap.sh -sur:minsize=1024,byname PID > /tmp/histo-sur.log
```

## 2.4 Prints object stats of old gen, live objects only:

```
./vjmap.sh -old:live PID > /tmp/histo-old.log
```

# 3.Outputs

## 3.1 Count Survivor Objects over the Age of 3.

```
Survivor Object Histogram:

 #num  #count     #bytes #Class description
-----------------------------------------------------------------------------------
   1:      37         1k io.netty.buffer.PoolThreadCache$MemoryRegionCache$Entry
   2:       2         64 java.util.concurrent.locks.AbstractQueuedSynchronizer$Node
Total: 39/    1k over age 2

Heap traversal took 1.3 seconds.
```



# 4. Eclipse MAT

如果只依靠对象统计信息，不足以定位问题，需要使用完整HeapDump，计算对象关联关系来进一步分析时，可以在MAT中使用OQL过滤出老生代的对象。

假设，OldGen地址范围是"0xfbd4c000" ～ "0xfce94050"

```
SELECT * FROM INSTANCEOF java.lang.Object t WHERE (toHex(t.@objectAddress) >= "0xfbd4c000" AND toHex(t.@objectAddress) <= "0xfce94050")
```

用如下方式可获得老生代地址：

第一种方式是在启动参数增加 -XX:+PrintHeapAtGC

第二种方式是使用vjmap的命令，在-old, -sur, -address 中，都会打印出区间的地址。 

```
./vjmap.sh -address PID

``` 

输出如下：
```
  eden [0x0000000119000000,0x0000000119c4a258,0x0000000121880000) space capacity = 143130624, 9.003395387977907 used
  from [0x0000000121880000,0x0000000121880000,0x0000000122990000) space capacity = 17891328, 0.0 used
  to   [0x0000000122990000,0x0000000122990000,0x0000000123aa0000) space capacity = 17891328, 0.0 used
concurrent mark-sweep generation
free-list-space[ 0x0000000123aa0000 , 0x0000000139000000 ) space capacity = 357957632 used(4%)= 17024696 free= 340932936
```


# 5. Enhancements over TBJMap
* Added JDK8 Support.
* Added Display: survivor objects over the specified age.
* Performance Boost: by accessing Survivor and OldGen directly instead of by accessing the whole heap with Heap Visitor callbacks.
* New config Arg: order objects by size and leave out small ones.
* New Config Arg: order objects by name for periodic comparison.
* Reading Friendliness: output by the unit of (k, m, g) and fix alignment, order objects in OldGen by size in OldGen view by default. 
