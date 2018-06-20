# 1. 概述

VJDump是线上JVM数据紧急收集脚本。 

它可以在紧急场景下（比如马上要对进程进行重启），一键收集jstack、jmap以及GC日志等相关信息，并以zip包保存(默认在目录`/tmp/vjtools/vjdump`下)，保证在紧急情况下仍能收集足够的问题排查信息，减轻运维团队的工作量，以及与开发团队的沟通成本。

收集数据包括：
* thread dump数据：`jstack -l $PID`
* vjtop JVM概况及繁忙线程：`vjtop.sh -n 1 $PID` (需要将vjtop.sh 加入用户的PATH变量中)
* jmap histo 堆对象统计数据：`jmap -histo $PID` & `jmap -histo:live $PID`
* GC日志(如果JVM有设定GC日志输出)
* heap dump数据（需指定--liveheap开启）：`jmap -dump:live,format=b,file=${DUMP_FILE} $PID`

# 2. 下载

[vjdump.sh](https://raw.githubusercontent.com/vipshop/vjtools/master/vjdump/vjdump.sh)

# 3. 快速入门

以目标JVM相同用户或root用户运行脚本：

```shell

# 对指定的进程PID进行急诊
vjdump.sh $pid


# 额外收集heap dump信息（jmap -dump:live的信息）
vjdump.sh --liveheap $pid

```


在收集过程中，某些命令如`jmap -histo:live $PID` 会造成JVM停顿，因此仅用于紧急情况或已摘流量的情况。为了避免连续停顿，在每条会造成停顿的收集指令之间，默认插入了1秒的执行间隔。
