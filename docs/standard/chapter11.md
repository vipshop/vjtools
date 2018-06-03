# (十一) 日志规约

**Rule 1. 【强制】应用中不可直接使用日志库（Log4j、Logback）中的API，而应使用日志框架SLF4J中的API**

使用门面模式的日志框架，有利于维护各个类的日志处理方式统一。

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static Logger logger = LoggerFactory.getLogger(Foo.class);
```

----  

**Rule 2. 【推荐】对不确定会否输出的日志，采用占位符或条件判断**

```java
//WRONG
logger.debug("Processing trade with id: " + id + " symbol: " + symbol);
```

如果日志级别是info，上述日志不会打印，但是会执行1)字符串拼接操作，2)如果symbol是对象，还会执行toString()方法，浪费了系统资源，最终日志却没有打印。

```java
//RIGHT
logger.debug("Processing trade with id: {} symbol : {} ", id, symbol);
```


但如果symbol.getMessage()本身是个消耗较大的动作，占位符在此时并没有帮助，须要改为条件判断方式来完全避免它的执行。

```java
//WRONG
logger.debug("Processing trade with id: {} symbol : {} ", id, symbol.getMessage());

//RIGHT
if (logger.isDebugEnabled()) {
  logger.debug("Processing trade with id: " + id + " symbol: " + symbol.getMessage());
}
```

----  

**Rule 3. 【推荐】对确定输出，而且频繁输出的日志，采用直接拼装字符串的方式**
  
如果这是一条WARN，ERROR级别的日志，或者确定输出的INFO级别的业务日志，直接字符串拼接，比使用占位符替换，更加高效。

Slf4j的占位符并没有魔术，每次输出日志都要进行占位符的查找，字符串的切割与重新拼接。

```java
//RIGHT
logger.info("I am a business log with id: " + id + " symbol: " + symbol);

//RIGHT
logger.warn("Processing trade with id: " + id + " symbol: " + symbol);
```

----  

**Rule 4. 【推荐】尽量使用异步日志**

低延时的应用，使用异步输出的形式(以AsyncAppender串接真正的Appender)，可减少IO造成的停顿。

需要正确配置异步队列长度及队列满的行为，是丢弃还是等待可用，业务上允许丢弃的尽量选丢弃。

----  

**Rule 5. 【强制】禁止使用性能很低的System.out()打印日志信息**

同理也禁止e.printStackTrace();

例外: 应用启动和关闭时，担心日志框架还未初始化或已关闭。

* [Sonar-106: Standard outputs should not be used directly to log anything](https://rules.sonarsource.com/java/RSPEC-106)
* [Sonar-1148: Throwable.printStackTrace(...) should not be called](https://rules.sonarsource.com/java/RSPEC-1148)

----  

**Rule 6. 【强制】禁止配置日志框架输出日志打印处的类名，方法名及行号的信息**

日志框架在每次打印时，通过主动获得当前线程的StackTrace来获取上述信息的消耗非常大，尽量通过Logger名本身给出足够信息。

----  

**Rule 7. 【推荐】谨慎地记录日志，避免大量输出无效日志，信息不全的日志**

大量地输出无效日志，不利于系统性能，也不利于快速定位错误点。

记录日志时请思考：这些日志真的有人看吗？看到这条日志你能做什么？能不能给问题排查带来好处？

---- 

**Rule 8. 【推荐】使用warn级别而不是error级别，记录外部输入参数错误的情况**

如非必要，请不在此场景打印error级别日志，避免频繁报警。

error级别只记录系统逻辑出错、异常或重要的错误信息。

----


