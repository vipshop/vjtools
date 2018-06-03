# (十) 异常处理

**Rule 1. 【强制】创建异常的消耗大，只用在真正异常的场景**

构造异常时，需要获得整个调用栈，有一定消耗。
    
不要用来做流程控制，条件控制，因为异常的处理效率比条件判断低。

发生概率较高的条件，应该先进行检查规避，比如：IndexOutOfBoundsException，NullPointerException等，所以如果代码里捕获这些异常通常是个坏味道。
    
```java
//WRONG
try { 
  return obj.method();
} catch (NullPointerException e) {
  return false;
}

//RIGHT
if (obj == null) {
  return false;
}     
```

* [Sonar-1696: "NullPointerException" should not be caught](https://rules.sonarsource.com/java/RSPEC-1696)

----

**Rule 2. 【推荐】在特定场景，避免每次构造异常**
   
如上，异常的构造函数需要获得整个调用栈。

如果异常频繁发生，且不需要打印完整的调用栈时，可以考虑绕过异常的构造函数。

1） 如果异常的message不变，将异常定义为静态成员变量;
  
下例定义静态异常，并简单定义一层的StackTrace。`ExceptionUtil`见vjkit。

```java
private static RuntimeException TIMEOUT_EXCEPTION = ExceptionUtil.setStackTrace(new RuntimeException("Timeout"),
MyClass.class, "mymethod");

...

throw TIMEOUT_EXCEPTION;
```


2） 如果异常的message会变化，则对静态的异常实例进行clone()再修改message。

Exception默认不是Cloneable的，`CloneableException`见vjkit。

```java
private static CloneableException TIMEOUT_EXCEPTION = new CloneableException("Timeout") .setStackTrace(My.class,
 "hello"); 
 
...

throw TIMEOUT_EXCEPTION.clone("Timeout for 40ms");
```


3）自定义异常，也可以考虑重载fillStackTrace()为空函数，但相对没那么灵活，比如无法按场景指定一层的StackTrace。

----

**Rule 3. 【推荐】自定义异常，建议继承`RuntimeException`**

详见《Clean Code》，争论已经结束，不再推荐原本初衷很好的CheckedException。

因为CheckedException需要在抛出异常的地方，与捕获处理异常的地方之间，层层定义throws XXX来传递Exception，如果底层代码改动，将影响所有上层函数的签名，导致编译出错，对封装的破坏严重。对CheckedException的处理也给上层程序员带来了额外的负担。因此其他语言都没有CheckedException的设计。

----

**Rule 4. 【推荐】异常日志应包含排查问题的足够信息**

异常信息应包含排查问题时足够的上下文信息。

捕获异常并记录异常日志的地方，同样需要记录没有包含在异常信息中，而排查问题需要的信息，比如捕获处的上下文信息。

```java
//WRONG
new TimeoutException("timeout");
logger.error(e.getMessage(), e);


//RIGHT
new TimeoutException("timeout:" + eclapsedTime + ", configuration:" + configTime);
logger.error("user[" + userId + "] expired:" + e.getMessage(), e);
```

* Facebook-Contrib: Style - Method throws exception with static message string

----

**Rule 5. 异常抛出的原则**


**5.1 【推荐】尽量使用JDK标准异常，项目标准异常**

尽量使用JDK标准的Runtime异常如`IllegalArgumentException`，`IllegalStateException`，`UnsupportedOperationException`，项目定义的Exception如`ServiceException`。


**5.2 【推荐】根据调用者的需要来定义异常类，直接使用`RuntimeException`是允许的**

是否定义独立的异常类，关键是调用者会如何处理这个异常，如果没有需要特别的处理，直接抛出RuntimeException也是允许的。

----

**Rule 6. 异常捕获的原则**

**6.1 【推荐】按需要捕获异常，捕获`Exception`或`Throwable`是允许的**

如果无特殊处理逻辑，统一捕获Exception统一处理是允许的。

捕获Throwable是为了捕获Error类异常，包括其实无法处理的`OOM` `StackOverflow` `ThreadDeath`，以及类加载，反射时可能抛出的`NoSuchMethodError` `NoClassDefFoundError`等。


**6.2【推荐】多个异常的处理逻辑一致时，使用JDK7的语法避免重复代码**

```java
try {
  ...
} catch (AException | BException | CException ex) {
  handleException(ex);
}
```

* [Sonar-2147: Catches should be combined](https://rules.sonarsource.com/java/RSPEC-2147)

----

**Rule 7.异常处理的原则**
    
**7.1 【强制】捕获异常一定要处理；如果故意捕获并忽略异常，须要注释写明原因**

方便后面的阅读者知道，此处不是漏了处理。

```java
//WRONG
try {
} catch(Exception e) {
}

//RIGHT
try {
} catch(Exception ignoredExcetpion) {
	//continue the loop
}
```

  
**7.2 【强制】异常处理不能吞掉原异常，要么在日志打印，要么在重新抛出的异常里包含原异常**

```java
 //WRONG
throw new MyException("message");

//RIGHT 记录日志后抛出新异常，向上次调用者屏蔽底层异常
logger.error("message", ex); 
throw new MyException("message"); 

//RIGHT 传递底层异常
throw new MyException("message", ex); 
```  

* [Sonar-1166: Exception handlers should preserve the original exceptions](https://rules.sonarsource.com/java/RSPEC-1166)，其中默认包含了InterruptedException, NumberFormatException，NoSuchMethodException等若干例外


**7.3 【强制】如果不想处理异常，可以不进行捕获。但最外层的业务使用者，必须处理异常，将其转化为用户可以理解的内容**

----

**Rule 8. finally块的处理原则**

**8.1 【强制】必须对资源对象、流对象进行关闭，或使用语法try-with-resource**

关闭动作必需放在finally块，不能放在try块 或 catch块，这是经典的错误。

更加推荐直接使用JDK7的try-with-resource语法自动关闭Closeable的资源，无需在finally块处理，避免潜在问题。

```java
try (Writer writer = ...) {
  writer.append(content);
}
```


**8.2 【强制】如果处理过程中有抛出异常的可能，也要做try-catch，否则finally块中抛出的异常，将代替try块中抛出的异常**

```java
//WRONG
try {
  ...
  throw new TimeoutException();
} finally {
  file.close();//如果file.close()抛出IOException, 将代替TimeoutException
}

//RIGHT, 在finally块中try－catch
try {
  ...
  throw new TimeoutException();
} finally {
  IOUtil.closeQuietly(file); //该方法中对所有异常进行了捕获
}
```

* [Sonar-1163: Exceptions should not be thrown in finally blocks](https://rules.sonarsource.com/java/RSPEC-1163)


**8.3 【强制】不能在finally块中使用return，finally块中的return将代替try块中的return及throw Exception**

```java
//WRONG
try {
  ...
  return 1;
} finally {
  return 2; //实际return 2 而不是1
}
	
try {
  ...
  throw TimeoutException();
} finally {
  return 2; //实际return 2 而不是TimeoutException
}
```

* [Sonar-1143: Jump statements should not occur in "finally" blocks](https://rules.sonarsource.com/java/RSPEC-1143)

----


