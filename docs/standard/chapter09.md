# (九) 并发处理

**Rule 1. 【强制】创建线程或线程池时请指定有意义的线程名称，方便出错时回溯**

1）创建单条线程时直接指定线程名称

```java
Thread t = new Thread();
t.setName("cleanup-thread");
```

2） 线程池则使用guava或自行封装的ThreadFactory，指定命名规则。

```java
//guava 或自行封装的ThreadFactory
ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();

ThreadPoolExecutor executor = new ThreadPoolExecutor(..., threadFactory, ...);
```

----

**Rule 2. 【推荐】尽量使用线程池来创建线程**

除特殊情况，尽量不要自行创建线程，更好的保护线程资源。

```java
//WRONG
Thread thread = new Thread(...);
thread.start();
```

同理，定时器也不要使用Timer，而应该使用ScheduledExecutorService。 

因为Timer只有单线程，不能并发的执行多个在其中定义的任务，而且如果其中一个任务抛出异常，整个Timer也会挂掉，而ScheduledExecutorService只有那个没捕获到异常的任务不再定时执行，其他任务不受影响。

----

**Rule 3. 【强制】线程池不允许使用 Executors去创建，避资源耗尽风险**

Executors返回的线程池对象的弊端 ：

1）FixedThreadPool 和 SingleThreadPool: 

允许的请求队列长度为 Integer.MAX_VALUE，可能会堆积大量的请求，从而导致 OOM。 


2）CachedThreadPool 和 ScheduledThreadPool:

允许的创建线程数量为 Integer.MAX_VALUE，可能会创建大量的线程，从而导致 OOM。


应通过 new ThreadPoolExecutor(xxx,xxx,xxx,xxx)这样的方式，更加明确线程池的运行规则，合理设置Queue及线程池的core size和max size，建议使用vjkit封装的ThreadPoolBuilder。

----

**Rule 4. 【强制】正确停止线程**

Thread.stop()不推荐使用，强行的退出太不安全，会导致逻辑不完整，操作不原子，已被定义成Deprecate方法。

停止单条线程，执行Thread.interrupt()。

停止线程池：

* ExecutorService.shutdown(): 不允许提交新任务，等待当前任务及队列中的任务全部执行完毕后退出；

* ExecutorService.shutdownNow(): 通过Thread.interrupt()试图停止所有正在执行的线程，并不再处理还在队列中等待的任务。

最优雅的退出方式是先执行shutdown()，再执行shutdownNow()，vjkit的`ThreadUtil`进行了封装。

注意，Thread.interrupt()并不保证能中断正在运行的线程，需编写可中断退出的Runnable，见规则5。

----

**Rule 5. 【强制】编写可停止的Runnable**

执行Thread.interrupt()时，如果线程处于sleep(), wait(), join(), lock.lockInterruptibly()等blocking状态，会抛出InterruptedException，如果线程未处于上述状态，则将线程状态设为interrupted。

因此，如下的代码无法中断线程:

```java
public void run() {

  while (true) { //WRONG，无判断线程状态。
    sleep();
  }

  public void sleep() {
    try {
	  Thread.sleep(1000);
	} catch (InterruptedException e) {
	  logger.warn("Interrupted!", e); //WRONG，吃掉了异常，interrupt状态未再传递
	}
  }
}
```


**5.1 正确处理InterruptException**

因为InterruptException异常是个必须处理的Checked Exception，所以run()所调用的子函数很容易吃掉异常并简单的处理成打印日志，但这等于停止了中断的传递，外层函数将收不到中断请求，继续原有循环或进入下一个堵塞。

正确处理是调用`Thread.currentThread().interrupt();` 将中断往外传递。

```java
//RIGHT
public void myMethod() {
  try {
    ...
  } catch (InterruptedException e) {
    Thread.currentThread().interrupt();
  }
}
```

* [Sonar-2142: "InterruptedException" should not be ignored](https://rules.sonarsource.com/java/RSPEC-2142)



**5.2 主循环及进入阻塞状态前要判断线程状态**

```java
//RIGHT
public void run() {
  try {
    while (!Thread.isInterrupted()) {
      // do stuff
    }
  } catch (InterruptedException e) {
    logger.warn("Interrupted!", e);
  }
}
```

其他如Thread.sleep()的代码，在正式sleep前也会判断线程状态。

----

**Rule 6. 【强制】Runnable中必须捕获一切异常**

如果Runnable中没有捕获RuntimeException而向外抛出，会发生下列情况：

1) ScheduledExecutorService执行定时任务，任务会被中断，该任务将不再定时调度，但线程池里的线程还能用于其他任务。

2) ExecutorService执行任务，当前线程会中断，线程池需要创建新的线程来响应后续任务。

3) 如果没有在ThreadFactory设置自定义的UncaughtExceptionHanlder，则异常最终只打印在System.err，而不会打印在项目的日志中。


因此建议自写的Runnable都要保证捕获异常; 如果是第三方的Runnable，可以将其再包裹一层vjkit中的SafeRunnable。

```java
executor.execute(ThreadPoolUtil.safeRunner(runner));
```

----

**Rule 7. 【强制】全局的非线程安全的对象可考虑使用ThreadLocal存放**

全局变量包括单例对象，static成员变量。

著名的非线程安全类包括SimpleDateFormat，MD5/SHA1的Digest。

对这些类，需要每次使用时创建。

但如果创建有一定成本，可以使用ThreadLocal存放并重用。

ThreadLocal变量需要定义成static，并在每次使用前重置。

```java
private static final ThreadLocal<MessageDigest> SHA1_DIGEST = new ThreadLocal<MessageDigest>() {
  @Override
  protected MessageDigest initialValue() {
    try {
	  return MessageDigest.getInstance("SHA");
	} catch (NoSuchAlgorithmException e) {
	  throw new RuntimeException("...", e);
	}
  }
};

public void digest(byte[] input) {
  MessageDigest digest = SHA1_DIGEST.get();
  digest.reset();
  return digest.digest(input);
}
```

* [Sonar-2885: Non-thread-safe fields should not be static](https://rules.sonarsource.com/java/RSPEC-2885)
* Facebook-Contrib: Correctness - Field is an instance based ThreadLocal variable

----

**Rule 8. 【推荐】缩短锁**

1） 能锁区块，就不要锁整个方法体；

```java
//锁整个方法，等价于整个方法体内synchronized(this)
public synchronized boolean foo(){};

//锁区块方法，仅对需要保护的原子操作的连续代码块进行加锁。
public boolean foo() {
	synchronized(this) {
		...
		...
	}
	//other stuff
}
```

2）能用对象锁，就不要用类锁。

```java
//对象锁，只影响使用同一个对象加锁的线程
synchronized(this) {
	...
}

//类锁，使用类对象作为锁对象，影响所有线程。
synchronized(A.class) {
	...
}
```

----

**Rule 10. 【推荐】选择分离锁，分散锁甚至无锁的数据结构**

* 分离锁：

1） 读写分离锁ReentrantReadWriteLock，读读之间不加锁，仅在写读和写写之间加锁；

2） Array Base的queue一般是全局一把锁，而Linked Base的queue一般是队头队尾两把锁。



* 分散锁（又称分段锁）：

1）如JDK7的ConcurrentHashMap，分散成16把锁；

2）对于经常写，少量读的计数器，推荐使用JDK8或vjkit封装的LongAdder对象性能更好（内部分散成多个counter，减少乐观锁的使用，取值时再相加所有counter）



* 无锁的数据结构： 

1）完全无锁无等待的结构，如JDK8的ConcurrentHashMap；

2）基于CAS的无锁有等待的数据结构，如AtomicXXX系列。

----

**Rule 11. 【推荐】基于ThreadLocal来避免锁**

比如Random实例虽然是线程安全的，但其实它的seed的访问是有锁保护的。因此建议使用JDK7的ThreadLocalRandom，通过在每个线程里放一个seed来避免了加锁。

----

**Rule 12. 【推荐】规避死锁风险**

对多个资源多个对象的加锁顺序要一致。

如果无法确定完全避免死锁，可以使用带超时控制的tryLock语句加锁。
    
----   

**Rule 13. 【推荐】volatile修饰符，AtomicXX系列的正确使用**

多线程共享的对象，在单一线程内的修改并不保证对所有线程可见。使用volatile定义变量可以解决（解决了可见性）。

但是如果多条线程并发进行基于当前值的修改，如并发的counter++，volatile则无能为力（解决不了原子性）。

此时可使用Atomic*系列:

```java
AtomicInteger count = new AtomicInteger(); 
count.addAndGet(2); 
```

但如果需要原子地同时对多个AtomicXXX的Counter进行操作，则仍然需要使用synchronized将改动代码块加锁。

----   

**Rule 14. 【推荐】延时初始化的正确写法**

通过双重检查锁（double-checked locking）实现延迟初始化存在隐患，需要将目标属性声明为volatile型，为了更高的性能，还要把volatile属性赋予给临时变量，写法复杂。

所以如果只是想简单的延迟初始化，可用下面的静态类的做法，利用JDK本身的class加载机制保证唯一初始化。

```java
private static class LazyObjectHolder {
  static final LazyObject instance = new LazyObject();
}

public void myMethod() {
  LazyObjectHolder.instance.doSomething();
}
```

* [Sonar-2168: Double-checked locking should not be used](https://rules.sonarsource.com/java/RSPEC-2168)

----


