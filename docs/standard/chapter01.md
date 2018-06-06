# (一) 命名规约

**Rule 1. 【强制】禁止拼音缩写，避免阅读者费劲猜测；尽量不用拼音, 尽量不要拼音与英文的混合。**

```text 
禁止： DZ[打折] / getPFByName() [评分]

尽量避免：Dazhe / DaZhePrice

```

----  

**Rule 2. 【强制】禁止使用非标准的英文缩写**

```text 
反例： AbstractClass 缩写成 AbsClass；condition 缩写成 condi。
```

----  
       
**Rule 3. 【强制】禁用其他编程语言风格的前缀和后缀**
 
在其它编程语言中使用的特殊前缀或后缀，如`_name`, `name_`, `mName`, `i_name`，在Java中都不建议使用。

----

**Rule 4. 【推荐】包名全部小写。点分隔符之间尽量只有一个英语单词，即使有多个单词也不使用下划线或大小写分隔**

```text 
正例： com.vip.javatool

反例： com.vip.java_tool, com.vip.javaTool
```
                                                                   
* [Sonar-120:Package names should comply with a naming convention](https://rules.sonarsource.com/java/RSPEC-120)

----  

**Rule 5. 【强制】类名与接口名使用UpperCamelCase风格，遵从驼峰形式**

Tcp, Xml等缩写也遵循驼峰形式，可约定例外如：DTO/ VO等。 

``` text
正例：UserId / XmlService / TcpUdpDeal / UserVO

反例：UserID / XMLService / TCPUDPDeal / UserVo
```

* [Sonar-101:Class names should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-101)
* [Sonar-114:Interface names should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-114)

---- 
 
**Rule 6. 【强制】方法名、参数名、成员变量、局部变量使用lowerCamelCase风格，遵从驼峰形式**

```text
正例： localValue / getHttpMessage();
```

* [Sonar-100:Method names should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-100)
* [Sonar-116:Field names should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-116)
* [Sonar-117:Local variable and method parameter names should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-117)

----  

**Rule 7. 【强制】常量命名全部大写，单词间用下划线隔开。力求语义表达完整清楚，不要嫌名字长**

```text
正例： MAX_STOCK_COUNT 
   
反例： MAX_COUNT
```
    
例外：当一个static final字段不是一个真正常量，比如不是基本类型时，不需要使用大写命名。
    
```java
//与其
private static final Logger LOGGER = Logger.getLogger(MyClass.class);

//不如
private static final Logger logger = Logger.getLogger(MyClass.class);
private static Logger logger = Logger.getLogger(MyClass.class);
```

* [Sonar-115:Constant names should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-115) Sonar会先判定是否基本类型
* [Sonar-308:Static non-final field names should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-308)

----  

**Rule 8. 【推荐】命名的好坏，在于其“模糊度”**

1）如果上下文很清晰，局部变量可以使用 `list` 这种简略命名， 否则应该使用 `userList` 这种更清晰的命名。


2）禁止 `a1, a2, a3` 这种带编号的没诚意的命名方式。


3）方法的参数名叫 `bookList` ，方法里的局部变量名叫 `theBookList` 也是很没诚意。


4）如果一个应用里同时存在 `Account、AccountInfo、AccountData` 类，或者一个类里同时有 `getAccountInfo()、getAccountData()`, `save()、 store()` 的函数，阅读者将非常困惑。


5） `callerId` 与 `calleeId`， `mydearfriendswithA` 与 `mydearfriendswithB` 这种拼写极度接近，考验阅读者眼力的。 

----  

**Rule 9. 【推荐】如果使用到了通用的设计模式，在类名中体现，有利于阅读者快速理解设计思想**

``` text
正例：OrderFactory， LoginProxy ，ResourceObserver
```

----      

**Rule 10. 【推荐】枚举类名以Enum结尾; 抽象类使用Abstract或Base开头；异常类使用Exception结尾；测试类以它要测试的类名开始，以Test结尾**

```text
正例：DealStatusEnum， AbstractView，BaseView， TimeoutException，UserServiceTest
```

* [Sonar-3577:Test classes should comply with a naming convention](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-3577)
* [Sonar-2166:Classes named like "Exception" should extend "Exception" or a subclass](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-2166)

----  

**Rule 11. 【推荐】实现类尽量用Impl的后缀与接口关联**
    
```text
正例：CacheServiceImpl 实现 CacheService接口。
```

----  

**Rule 12. 【强制】POJO类中布尔类型的变量名，不要加is前缀，否则部分框架解析会引起序列化错误**

反例：Boolean isSuccess的成员变量，它的GET方法也是isSuccess()，部分框架在反射解析的时候，“以为”对应的成员变量名称是success，导致出错。

----

**Rule 13. 【强制】避免成员变量，方法参数，局部变量重名复写，避免引起混淆**

* 类的私有成员变量名，不与父类的成员变量重名

* 方法的参数名/局部变量名，不与类的成员变量重名(getter/setter例外)

下面错误的地方，在编译时都是合法的，但给阅读者带来极大的障碍。

```java
public class A {
  int foo;
}

public class B extends A {
  int foo; //WRONG
  int bar;

  public void hello(int bar) { //WRONG
    int foo = 0; //WRONG
  }

  public void setBar(int bar) { //OK
    this.bar = bar;
  }
}
```

* [Sonar-2387: Child class fields should not shadow parent class fields](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-2387)
* [Sonar: Local variables should not shadow class fields](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-1117)

----

