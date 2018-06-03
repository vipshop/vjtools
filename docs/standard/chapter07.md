# (七) 基本类型与字符串

**Rule 1. 原子数据类型(int等)与包装类型(Integer等)的使用原则**    

**1.1 【推荐】需要序列化的POJO类属性使用包装数据类型** 


**1.2 【推荐】RPC方法的返回值和参数使用包装数据类型** 


**1.3 【推荐】局部变量尽量使用基本数据类型**

    
包装类型的坏处:  

1）Integer 24字节，而原子类型 int 4字节。

2）包装类型每次赋予还需要额外创建对象，除非在缓存区(见Integer.IntegerCache与Long.LongCache)，Integer var = ?在缓存区间的赋值，会复用h缓存对象。默认缓存区间为-127到128，受启动参数的影响，如-XX:AutoBoxCacheMax=20000。

3）包装类型还有==比较的陷阱（见规则3）


包装类型的好处:

1）包装类型能表达Null的语义。 
    
比如数据库的查询结果可能是null，如果用基本数据类型有NPE风险。又比如显示成交总额涨跌情况，如果调用的RPC服务不成功时，应该返回null，显示成-%，而不是0%。

2）集合需要包装类型，除非使用数组，或者特殊的原子类型集合。
    
3）泛型需要包装类型，如`Result<Integer>`。
    
----  

**Rule 2.原子数据类型与包装类型的转换原则**  

**2.1【推荐】自动转换(AutoBoxing)有一定成本，调用者与被调用函数间尽量使用同一类型，减少默认转换**

```java
//WRONG, sum 类型为Long， i类型为long，每次相加都需要AutoBoxing。
Long sum=0L;

for( long i = 0; i < 10000; i++) {
  sum+=i;
}

//RIGHT, 准确使用API返回正确的类型
Integer i = Integer.valueOf(str);
int i = Integer.parseInt(str);
```

* [Sonar-2153: Boxing and unboxing should not be immediately reversed](https://rules.sonarsource.com/java/RSPEC-2153)


**2.2 【推荐】自动拆箱有可能产生NPE，要注意处理**

```java
//如果intObject为null，产生NPE
int i = intObject; 
```

----  

**Rule 3. 数值equals比较的原则**

**3.1【强制】 所有包装类对象之间值的比较，全部使用equals方法比较**
      
\==判断对象是否同一个。Integer var = ?在缓存区间的赋值（见规则1），会复用已有对象，因此这个区间内的Integer使用 \==进行判断可通过，但是区间之外的所有数据，则会在堆上新产生，不会通过。因此如果用\== 来比较数值，很可能在小的测试数据中通过，而到了生产环境才出问题。


**3.2【强制】 BigDecimal需要使用compareTo()**

因为BigDecimal的equals()还会比对精度，2.0与2.00不一致。

* Facebook-Contrib: Correctness - Method calls BigDecimal.equals()


**3.3【强制】 Atomic* 系列，不能使用equals方法**

因为 Atomic* 系列没有覆写equals方法。

```java
//RIGHT
if (counter1.get() == counter2.get()){...}
```

* [Sonar-2204: ".equals()" should not be used to test the values of "Atomic" classes](https://rules.sonarsource.com/java/RSPEC-2204)


**3.4【强制】 double及float的比较，要特殊处理**

因为精度问题，浮点数间的equals非常不可靠，在vjkit的NumberUtil中有对应的封装函数。

```java
float f1 = 0.15f;
float f2 = 0.45f/3; //实际等于0.14999999

//WRONG
if (f1 == f2) {...}
if (Double.compare(f1,f2)==0) 

//RIGHT
static final float EPSILON = 0.00001f;
if (Math.abs(f1-f2)<EPSILON) {...}
```

* [Sonar-1244: Floating point numbers should not be tested for equality](https://rules.sonarsource.com/java/RSPEC-1244)

----  

**Rule 4. 数字类型的计算原则**

**4.1【强制】数字运算表达式，因为先进行等式右边的运算，再赋值给等式左边的变量，所以等式两边的类型要一致**
 
例子1: int与int相除后，哪怕被赋值给float或double，结果仍然是四舍五入取整的int。 

需要强制将除数或被除数转换为float或double。
   
```java
double d = 24/7;  //结果是3.0
double d =  (double)24/7; //结果是正确的3.42857
```

例子2： int与int相乘，哪怕被赋值给long，仍然会溢出。

需要强制将乘数的一方转换为long。

```java
long l = Integer.MAX_VALUE * 2; // 结果是溢出的－2
long l = Integer.MAX_VALUE * 2L; //结果是正确的4294967294
```

另外，int的最大值约21亿，留意可能溢出的情况。

* [Sonar-2184: Math operands should be cast before assignment](https://rules.sonarsource.com/java/RSPEC-2184)


**4.2【强制】数字取模的结果不一定是正数，负数取模的结果仍然负数**

取模做数组下标时，如果不处理负数的情况，很容易ArrayIndexOutOfBoundException。

另外，Integer.MIN_VALUE取绝对值也仍然是负数。因此，vjkit的MathUtil对上述情况做了安全的封装。

```java
-4 % 3  = -1;
Math.abs(Integer.MIN_VALUE) = -2147483648;
```

* Findbugs: Style - Remainder of hashCode could be negative


**4.3【推荐】 double 或 float 计算时有不可避免的精度问题**

```java

float f = 0.45f/3;    //结果是0.14999999

double d1 = 0.45d/3;  //结果是正确的0.15

double d2 = 1.03d - 0.42d; //结果是0.6100000000000001

```

尽量用double而不用float，但如果是金融货币的计算，则必须使用如下选择：

选项1， 使用性能较差的BigDecimal。BigDecimal还能精确控制四舍五入或是其他取舍的方式。

选项2， 在预知小数精度的情况下，将浮点运算放大为整数计数，比如货币以"分"而不是以"元"计算。


* [Sonar-2164: Math should not be performed on floats](https://rules.sonarsource.com/java/RSPEC-2164)

----  

**Rule 5. 【推荐】如果变量值仅有有限的可选值，用枚举类来定义常量**

尤其是变量还希望带有名称之外的延伸属性时，如下例：
  
```java
//WRONG
public String MONDAY = "SPRING";
public int MONDAY_SEQ = 1;

//RIGHT
public enum SeasonEnum { 
	SPRING(1), SUMMER(2), AUTUMN(3), WINTER(4); 
	int seq; 
	SeasonEnum(int seq) { this.seq = seq; }
}
```

业务代码中不要依赖ordinary()函数进行业务运算，而是自定义数字属性，以免枚举值的增减调序造成影响。 例外：永远不会有变化的枚举，比如上例的一年四季。

----  

**Rule 6. 字符串拼接的原则**

**6.1 【推荐】 当字符串拼接不在一个命令行内写完，而是存在多次拼接时(比如循环)，使用StringBuilder的append()**

```java
String s  = "hello" + str1 +  str2;  //Almost OK，除非初始长度有问题，见第3点.

String s  = "hello";  //WRONG
if (condition) {
  s += str1;
}

String str = "start";       //WRONG
for (int i = 0; i < 100; i++) {
  str = str + "hello";
}
```

反编译出的字节码文件显示，其实每条用`+`进行字符拼接的语句，都会new出一个StringBuilder对象，然后进行append操作，最后通过toString方法返回String对象。所以上面两个错误例子，会重复构造StringBuilder，重复toString()造成资源浪费。


* [Sonar-1643: Strings should not be concatenated using '+' in a loop](https://rules.sonarsource.com/java/RSPEC-1643)


**6.2 【强制】 字符串拼接对象时，不要显式调用对象的toString()**

如上，`+`实际是StringBuilder，本身会调用对象的toString()，且能很好的处理null的情况。

```java
//WRONG
str = "result:" + myObject.toString();  // myObject为Null时，抛NPE

//RIGHT
str = "result:" + myObject;  // myObject为Null时，输出 result:null
```


**6.3【强制】使用StringBuilder，而不是有所有方法都有同步修饰符的StringBuffer**

因为内联不成功，逃逸分析并不能抹除StringBuffer上的同步修饰符

* [Sonar-1149: Synchronized classes Vector, Hashtable, Stack and StringBuffer should not be used](https://rules.sonarsource.com/java/RSPEC-1149)


**6.4 【推荐】当拼接后字符串的长度远大于16时，指定StringBuilder的大概长度，避免容量不足时的成倍扩展**


**6.5 【推荐】如果字符串长度很大且频繁拼接，可考虑ThreadLocal重用StringBuilder对象**

参考BigDecimal的toString()实现，及vjkit中的StringBuilderHolder。

----  

**Rule 7. 【推荐】字符操作时，优先使用字符参数，而不是字符串，能提升性能**

```java
//WRONG
str.indexOf("e");

//RIGHT
stringBuilder.append('a'); 
str.indexOf('e');
str.replace('m','z');
```

其他包括split等方法，在JDK String中未提供针对字符参数的方法，可考虑使用Apache Commons StringUtils 或Guava的Splitter。

* [Sonar-3027: String function use should be optimized for single characters](https://rules.sonarsource.com/java/RSPEC-3027)

----  

**Rule 8. 【推荐】利用好正则表达式的预编译功能，可以有效加快正则匹配速度**
       
反例：
```java
//直接使用String的matches()方法
result = "abc".matches("[a-zA-z]");

//每次重新构造Pattern
Pattern pattern = Pattern.compile("[a-zA-z]");
result = pattern.matcher("abc").matches();
```

正例：  
```java
//在某个地方预先编译Pattern，比如类的静态变量
private static Pattern pattern = Pattern.compile("[a-zA-z]");
...
//真正使用Pattern的地方
result = pattern.matcher("abc").matches();
```

----


