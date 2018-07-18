# 容器中JVM获取真实的CPU核数

基于 [libsysconfcpus](https://github.com/obmarg/libsysconfcpus)的方案，可以为各个版本的JDK提供一个通用的解决方案。

libsysconfcpus.so的原理是截获JVM获取CPU核数所用的系统调用sysconf(_SC_NPROCESSORS_CONF)，改为读取环境变量LIBSYSCONFCPUS返回。

首先，从[libsysconfcpus](https://github.com/obmarg/libsysconfcpus)获取并编译so文件，放入镜像中。

然后，编写类似的脚本，完成两件事情：

1. 定义环境变量LD_PRELOAD，将libsysconfcpus.so放在最前面达到截获的目的。

2. 我们的系统在部署容器的时候，会额外传入一个环境变量"CONTAINER_CORE_LIMIT"代表分配的CPU核数(需按自己的情况修改)，脚本将其转换为libsysconfcpus所需的环境变量。

注意：当JVM是以-server启动时，至少需要2核，否则在启动时会被死锁。

```
#!/bin/sh

if [ "x$CONTAINER_CORE_REQUEST" != "x" ]; then
   LIBSYSCONFCPUS="$CONTAINER_CORE_REQUEST"  
   if [ ${LIBSYSCONFCPUS} -lt 2 ]; then
      LIBSYSCONFCPUS=2
   fi
   export LIBSYSCONFCPUS      
fi
export LD_PRELOAD="/usr/local/lib/libsysconfcpus.so:$LD_PRELOAD"
```


