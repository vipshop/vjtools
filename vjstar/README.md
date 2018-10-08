# 1. 概述

服务化应用的性能、可用性的最佳实践封装，主要体现思路。

# 2. 实践列表

## 2.1 JVM启动参数

参数兼顾性能及排查问题的便捷性的JVM启动参数推荐， 其中一些参数需要根据JDK版本适配。

源码： [jvm-options](https://github.com/vipshop/vjtools/blob/master/vjstar/src/main/script/jvm-options)
解读：[《关键业务系统的JVM参数推荐》](http://calvin1978.blogcn.com/?p=1602)

## 2.2 容器中JVM获取CPU核数的通用补丁

容器中的JVM，获取的仍然是宿主机的CPU核数，从而引起GC线程数，Netty线程数等一系列混乱。据说JDK8的最新版解决了这个问题，但其他版本的JDK则建议使用此补丁。
基于[libsysconfcpus](https://github.com/obmarg/libsysconfcpus)，详见[docker-cpus](https://github.com/vipshop/vjtools/blob/master/vjstar/src/main/script/docker-cpus)。

## 2.3 闲时主动GC

CMS GC 始终对流量有一定的影响。

因此我们希望在夜半闲时，如果检测到老生代已经达到50%， 则主动进行一次GC。

简单的定时器让应用固定在可设定的闲时（如半夜）进行清理动作。 为了避免服务的所有实例同时清理造成服务不可用，加入了随机值。

详见[Proactive GC](https://github.com/vipshop/vjtools/tree/master/vjstar/src/main/java/com/vip/vjstar/gc)

## 2.4 滑动窗口计数器（试验）

滑动窗口的计数器（比如任意时刻的最近一分钟请求数）在熔断计算等方面的使用很广泛，但没有比较标准且抽象成通用类库的实现，我们在考察了几家实现的实现。

详见[Sliding Window](https://github.com/vipshop/vjtools/tree/master/vjstar/src/main/java/com/vip/vjstar/window)(试验性的新方案，还没替换生产上的旧方案)

## 2.5 动态隔离线程池(TODO)

我们希望一个业务方法缓慢时，不会把整个线程池塞爆导致所有方法都不能响应。

但是为每个方法配置独立线程池又存在配置困难和浪费问题，因此我们希望简单实现一个线程池，在平时使用公共池，当某个方法出现问题时对其进行隔离，当问题消失时又自动恢复到公共池。


