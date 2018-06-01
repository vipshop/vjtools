# VJtop 
 
VJtop is a collection of  Linux top-like JVM info and busy threads monitoring tools.

# 1. Introduction

We believe you are good with linux top command. That's why we present VJtop for JVM monitoring for 
CPU/Memory intensive threads. Develop on top of the [jvmtop](https://github.com/patric-r/jvmtop) project, 
we also incorporate advantages of [SJK](https://github.com/aragozin/jvm-tools) project so that
 a variety of information is collected from /proc, PerfData and JMX in a performant way. 

VJtop is built as NON-stop-the-world and is considered production-safe.


# 2. Getting Started

## 2.1 How to build

Run Maven install task and unzip the resulted zip package in target folder. JAVA_HOME environment variable is needed.

Run the following command under **the same user who started the process to be monitored**. If privileges error are met, 
try with root user.

```
// showing threads consuming the most cpu time
./vjtop.sh <PID>
```

## 2.2 How it works：

### 2.21 Sources of Process Stats

Process data are retrieved from the following sources:

*   from /proc/PID/*
*   from/tmp/perfxxxx, where stats are written by JDK every other second
*   through JMX of the targeted VM. (If JMX were not started in the target process, 
VJtop would attach to the process to start it.)

[Note]: If the same item appear in both PerfData and JMX, that in PerfData is perferred. Data in JMX is used when 
PerfData is unavailable.


### 2.2.2 Sources of Thread Stats

With ThreadMxBean:

1. getAllThreadIds() is called to collect Thread Ids
2. getThreadCpuTime(tids) is called to get all thread cpu time as well as SYS CPU time and mem allocation.
3. getThreadInfo(tids) is called, showing the top 10 threads. StackTrace is not fetched and the application will not stop. 

## 2.3 Spot the Busiest Threads.

### 2.3.1 Commands:

```
// rank threads by their cpu time, by default, the top 10 are shown and refreshed in every 10 secs
./vjtop.sh <PID>

// rank threads by total cpu time since startup, differentiated from cpu time within the output interval
./vjtop.sh --totalcpu <PID>

// rank threads by sys cpu
./vjtop.sh --syscpu <PID>

// rank threads by total sys cpu
./vjtop.sh --totalsyscpu <PID>
```

### 2.3.2 Outputs：

```
 VJTop 1.0.0 - 11:38:02, UPTIME: 3d01h
 PID: 127197, JVM: 1.7.0_79, USER: even.liang
 PROCESS:  0.99% cpu ( 0.04% of 24 core), 2491m rss,   0m swap
 IO:   24k rchar,    1k wchar,    0 read_bytes,    0 write_bytes
 THREAD:   97 active,   89 daemon,   99 peak,  461 created, CLASS: 12243 loaded, 0 unloaded
 HEAP: 160m/819m eden, 0m/102m sur, 43m/1024m old
 NON-HEAP: 55m/256m cms perm gen, 8m/96m codeCache
 OFF-HEAP: 0m/0m direct, 0m/0m map
 GC: 0/0ms ygc, 0/0ms fgc, SAFE-POINT: 6 count, 1ms time, 1ms syncTime
 THREADS-CPU:  1.01% (user= 0.31%, sys= 0.70%)

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

 Note: Only top 10 threads (according cpu load) are shown!
 Cost time:  46ms, CPU time:  60ms
```
Process Region Explained:

* `rss`: `Resident Set Size`, size of all the pages, fetched from /proc/<pid>/status, for definition see [proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)。
* `swap`: Size of pages that are swapped out。fetched from /proc/<pid>/status, for definition see [proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)。
* `rchar/wchar`: Number of bytes read or written with system calls。fetched from /proc/<pid>/io，for definition see [proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)。
* `read_bytes/write_bytes`: Bytes read from or written to the actual storage layer 。fetched from/proc/<pid>/io，for definition see[proc filesystem](http://man7.org/linux/man-pages/man5/proc.5.html)。
* `codeCache`: Cache size holding binaries as result of JIT compiling. Compiling cannot continue when it is full.
* `direct`: Off-heap memory usage. Note that off-heap usage could not be recorded for recent Netty versions, which bypass the JDK API for memory allocation。
* `SAFE-POINT`: JVM real stop counts and stop time, Collected only when PerfData is available. 

Thread Region Explained: 

* `CPU`: cpu time by percentage within the output interval (100% per core).
* `SYSCPU`: sys cpu time by percentage within the output interval (100% per core).
* `TOTAL`:  thread/process total cpu time by percentage since start up.  
* `TOLSYS`: total sys cpu time by percentage.

Bottom Region Explained :

* `Cost time`: For this run, time cost for data gathering & output 
* `CPU time`: For this run, cpu time cost for data gathering & ouput

## 2.4 Spot Threads with the most Frequent Mem Allocation

### 2.4.1 Commands

```
// Order threads by memory allocation rates, by default, the top 10 are shown and refreshed in every 10 secs
./vjtop.sh --memory <PID>

// Order Threads by total memory allcoation rates since startup instead of by interval
./vjtop.sh --totalmemory <PID>
```

### 2.4.2 Outputs

```
(headers omitted)
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
Process Region Explained:
* `allocation rate`: Total Memory Allocation speed by all threads.

Bottom Region Explained:

* `STATE`: current thread state
* `MEMORY`: Instant memory allocation rate by size per second (Instant memory allocation by this thread per second, divided by total allocation at the same time)
* `TOTAL-ALLOCATED`: Accumulated memory allocation since startup, including that recycled (Accumulated memory by this thread, divide by total memory allocation, in percent)

## 2.5 Common Args

```
// print other options
./vjtop.sh -h

// output to file
./vjtop.sh <PID> > /tmp/vjtop.log

// refresh in every 5 secs (default is 10 secs)
./vjtop.sh -d 5 <PID>

// show the top 20 threads（default is top 10）
./vjtop.sh -l 20 <PID>

// print by width of 120 characters（default is 100）
./vjtop.sh -w 120 <PID> > /tmp/vjtop.log

// quit after 20 printings
./vjtop.sh -n 20 <PID>
```

# 3. Improvements over The Original jvmtop

### 3.1 Hot Thread Pages：

* Added Feature: thread memory allocation rankings (from SJK).
* Added Feature: thread sys cpu time rankings, total cpu time since startup rankings (from SJK).
* Added Feature: Thread physical memory, swapness, IO stats .
* Added Feature: **per heap generation memory and GC information display**, CodeCache and off-heap memory display. 
* Added Config Arg: Printing interval, number of thread to display.
* Performance optimization: **Reduce time cost by up to 80%**, fetch thread cpu time in batch (inspired by SJK).   

### 3.2 Optimization for Production
* Remove profile page that cause STW from jvmtop.
* Eliminate the steps to fetch all java threads in JVM TOP. Overview page, in which results are uncertain, is also removed. 
* Default output interval set to 10 secs.
* Print **cost of the monitoring tool itself**.
