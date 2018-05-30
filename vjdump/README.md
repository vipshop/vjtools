# 1. 概述

[vjdump](https://github.com/vipshop/vjtools/tree/master/vjdump)是线上紧急数据收集脚本。 

它可以在紧急场景下（比如马上要对进程进行重启），一键收集jstack、jmap以及GC日志等相关信息，并以zip包保存。zip包默认放在目录`/tmp/vjtools/vjdump`下。
这个脚本能减轻运维团队的工作量，减少线上排查问题时的沟通时间和成本。

vjdump收集的数据包括：
* threaddump数据：`jstack -l $PID`
* vjtop数据：`vjtop.sh -n 1 -d 3 $PID`
* jmap histo数据：`jmap -histo $PID` & `jmap -histo:live $PID`
* live heap dump数据（需指定--liveheap开启）：`jmap -dump:live,format=b,file=${JMAP_DUMP_FILE} $PID`

# 2. 下载

[vjdump.sh](https://github.com/vipshop/vjtools/blob/master/vjdump/vjdump.sh)

# 3. 快速入门

运行脚本：

```shell

# 对指定的进程PID进行急诊
vjdump.sh $pid

# 对指定的进程PID进行急诊，并指定每条命令的间隔时间(-i, --interval)为2s（默认是1s）
vjdump.sh -i 2 $pid 

# 对指定的进程PID进行急诊，额外收集heap dump信息（jmap -dump:live的信息）
vjdump.sh --liveheap $pid

```