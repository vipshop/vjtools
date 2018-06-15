# VJDump
VJDump comes as a handy script for collecting diagnostic data for JVM during urgent failures to
**allow for complete analysis later**.

# 1.Introduction
When major system failures occur for reasons not yet known, rebooting may be the only option to ease user complaints.
There is so little time that even an experienced system admin might forget to pick up all he needs for offline analysis. 

VJDump script is written to make us eaiser, which just packs outputs from T
jstack, jmap, gc logs into the form of a single zip file under `/tmp/vjtools/vjdump`.

**[Important]**: Commands like jstack and jmap DO cause stop-of-the-world of the target app. Make sure the target app 
is isolated from user access before you run this full check in production.

Items to be collected by VJDump are:
* thread dump via `jstack -l $PID`
* vjtop JVM overview and busy threads snapshot via  `vjtop.sh -n 1 -d 3 $PID` (enabled when you have our vjtop installed and 
have the folder of vjtop.sh appended to the PATH environment variable)
* jmap histo object statistics via`jmap -histo $PID` & `jmap -histo:live $PID`
* GC logs if available
* heap dump (which can be optionally switched on via --liveheap)：`jmap -dump:live,format=b,file=${DUMP_FILE} $PID`

# 2. Download
[vjdump.sh](https://raw.githubusercontent.com/vipshop/vjtools/master/vjdump/vjdump.sh)

# 3. Getting Started
Use the following commands under **the same user who started the target process** and collect results in `/tmp/vjtools/vjdump`.

```shell

# collect dignostics for target $pid
vjdump.sh $pid


# include heap dump also via jmap -dump:live, might take longer
vjdump.sh --liveheap $pid

```
