# (五) 类设计

**Rule 1. 【推荐】类成员与方法的可见性最小化**

任何类、方法、参数、变量，严控访问范围。过于宽泛的访问范围，不利于模块解耦。思考：如果是一个private的方法，想删除就删除，可是一个public的service方法，或者一个public的成员变量，删除一下，不得手心冒点汗吗？
  
例外：为了单元测试，有时也可能将访问范围扩大，此时需要加上JavaDoc说明或vjkit中的`@VisibleForTesting`注解。

---- 

**Rule 2.【推荐】 减少类之间的依赖**

比如如果A类只依赖B类的某个属性，在构造函数和方法参数中，只传入该属性。让阅读者知道，A类只依赖了B类的这个属性，而不依赖其他属性，也不会调用B类的任何方法。

```java
a.foo(b);     //WRONG

a.foo(b.bar); //RIGHT
```
----    

**Rule 3.【推荐】 定义变量与方法参数时，尽量使用接口而不是具体类**

使用接口可以保持一定的灵活性，也能向读者更清晰的表达你的需求：变量和参数只是要求有一个Map，而不是特定要求一个HashMap。
    
例外：如果变量和参数要求某种特殊类型的特性，则需要清晰定义该参数类型，同样是为了向读者表达你的需求。

----
      
**Rule 4. 【推荐】类的长度度量**
    
类尽量不要超过300行，或其他团队共同商定的行数。

对过大的类进行分拆时，可考虑其内聚性，即类的属性与类的方法的关联程度，如果有些属性没有被大部分的方法使用，其内聚性是低的。

----  

**Rule 5.【推荐】 构造函数如果有很多参数，且有多种参数组合时，建议使用Builder模式**

```java
Executor executor = new ThreadPoolBuilder().coreThread(10).queueLenth(100).build();
```

即使仍然使用构造函数，也建议使用chain constructor模式，逐层加入默认值传递调用，仅在参数最多的构造函数里实现构造逻辑。

```java
public A(){
  A(DEFAULT_TIMEOUT);
}

public A(int timeout) {
  ...
}
```

----  

**Rule 6.【推荐】构造函数要简单，尤其是存在继承关系的时候**

可以将复杂逻辑，尤其是业务逻辑，抽取到独立函数，如init()，start()，让使用者显式调用。

```java
Foo foo = new Foo();
foo.init();
```

----  

**Rule 7.【强制】所有的子类覆写方法，必须加`@Override`注解**

比如有时候子类的覆写方法的拼写有误，或方法签名有误，导致没能真正覆写，加`@Override`可以准确判断是否覆写成功。

而且，如果在父类中对方法签名进行了修改，子类会马上编译报错。

另外，也能提醒阅读者这是个覆写方法。
    
最后，建议在IDE的Save Action中配置自动添加`@Override`注解，如果无意间错误同名覆写了父类方法也能被发现。

* [Sonar-1161: "@Override" should be used on overriding and implementing methods](https://rules.sonarsource.com/java/RSPEC-1161)

----

**Rule 8.【强制】静态方法不能被子类覆写。**

因为它只会根据表面类型来决定调用的方法。
    
```java
Base base = new Children();

// 下句实际调用的是父类的静态方法，虽然对象实例是子类的。
base.staticMethod();
```

----  

**Rule 9.静态方法访问的原则**
 
**9.1【推荐】避免通过一个类的对象引用访问此类的静态变量或静态方法，直接用类名来访问即可**

目的是向读者更清晰传达调用的是静态方法。可在IDE的Save Action中配置自动转换。

```java
int i = objectA.staticMethod(); // WRONG

int i = ClassA.staticMethod(); // RIGHT
```

* [Sonar-2209: "static" members should be accessed statically](https://rules.sonarsource.com/java/RSPEC-2209)
* [Sonar-2440: Classes with only "static" methods should not be instantiated](https://rules.sonarsource.com/java/RSPEC-2440)


**9.2 【推荐】除测试用例，不要static import 静态方法**

静态导入后忽略掉的类名，给阅读者造成障碍。

例外：测试环境中的assert语句，大家都太熟悉了。

* [Sonar-3030: Classes should not have too many "static" imports](https://rules.sonarsource.com/java/RSPEC-3030) 但IDEA经常自动转换static import，所以暂不作为规则。


**9.3【推荐】尽量避免在非静态方法中修改静态成员变量的值**

```java
// WRONG
public void foo() {
  ClassA.staticFiled = 1;
}
```

* [Sonar-2696: Instance methods should not write to "static" fields](https://rules.sonarsource.com/java/RSPEC-2696)
* [Sonar-3010: Static fields should not be updated in constructors](https://rules.sonarsource.com/java/RSPEC-3010)


----  

**Rule 10.【推荐】 内部类的定义原则**

当一个类与另一个类关联非常紧密，处于从属的关系，特别是只有该类会访问它时，可定义成私有内部类以提高封装性。

另外，内部类也常用作回调函数类，在JDK8下建议写成Lambda。
    
内部类分匿名内部类，内部类，静态内部类三种。
    
1) 匿名内部类 与 内部类，按需使用：

在性能上没有区别；当内部类会被多个地方调用，或匿名内部类的长度太长，已影响对调用它的方法的阅读时，定义有名字的内部类。


2) 静态内部类 与 内部类，优先使用静态内部类：

1. 非静态内部类持有外部类的引用，能访问外类的实例方法与属性。构造时多传入一个引用对性能没有太大影响，更关键的是向阅读者传递自己的意图，内部类会否访问外部类。
2. 非静态内部类里不能定义static的属性与方法。
 
* [Sonar-2694: Inner classes which do not reference their owning classes should be "static"](https://rules.sonarsource.com/java/RSPEC-2694)
* [Sonar-1604: Anonymous inner classes containing only one method should become lambdas](https://rules.sonarsource.com/java/RSPEC-1604)

----  

**Rule 11.【推荐】使用getter/setter方法，还是直接public成员变量的原则。**

除非因为特殊原因方法内联失败，否则使用getter方法与直接访问成员变量的性能是一样的。

使用getter/setter，好处是可以进一步的处理：

1. 通过隐藏setter方法使得成员变量只读

2. 增加简单的校验逻辑

3. 增加简单的值处理，值类型转换等

建议通过IDE生成getter/setter。
    
但getter/seter中不应有复杂的业务处理，建议另外封装函数，并且不要以getXX/setXX命名。


如果是内部类，以及无逻辑的POJO/VO类，使用getter/setter除了让一些纯OO论者感觉舒服，没有任何的好处，建议直接使用public成员变量。

例外：有些序列化框架只能从getter/setter反射，不能直接反射public成员变量。

----  

**Rule 12.【强制】POJO类必须覆写toString方法。**
 
便于记录日志，排查问题时调用POJO的toString方法打印其属性值。否则默认的Object.toString()只打印`类名@数字`的无效信息。

----  

**Rule 13. hashCode和equals方法的处理，遵循如下规则:**


**13.1【强制】只要重写equals，就必须重写hashCode。 而且选取相同的属性进行运算。**


**13.2【推荐】只选取真正能决定对象是否一致的属性，而不是所有属性，可以改善性能。**


**13.3【推荐】对不可变对象，可以缓存hashCode值改善性能（比如String就是例子）。**


**13.4【强制】类的属性增加时，及时重新生成toString，hashCode和equals方法。**


* [Sonar-1206: "equals(Object obj)" and "hashCode()" should be overridden in pairs](https://rules.sonarsource.com/java/RSPEC-1206)

----  

**Rule 14.【强制】使用IDE生成toString，hashCode和equals方法。**

使用IDE生成而不是手写，能保证toString有统一的格式，equals和hashCode则避免不正确的Null值处理。

子类生成toString() 时，还需要勾选父类的属性。

----

**Rule 15. 【强制】Object的equals方法容易抛空指针异常，应使用常量或确定非空的对象来调用equals**    

推荐使用java.util.Objects#equals（JDK7引入的工具类）

```java
"test".equals(object);  //RIGHT

Objects.equals(object, "test"); //RIGHT
```

* [Sonar-1132: Strings literals should be placed on the left side when checking for equality](https://rules.sonarsource.com/java/RSPEC-1132)

----  

**Rule 16.【强制】除了保持兼容性的情况，总是移除无用属性、方法与参数**

特别是private的属性、方法、内部类，private方法上的参数，一旦无用立刻移除。信任代码版本管理系统。

* [Sonar-3985: Unused "private" classes should be removed](https://rules.sonarsource.com/java/RSPEC-3985)
* [Sonar-1068: Unused "private" fields should be removed](https://rules.sonarsource.com/java/RSPEC-1068)
* [Sonar: Unused "private" methods should be removed](https://rules.sonarsource.com/java/RSPEC-1144)
* [Sonar-1481: Unused local variables should be removed](https://rules.sonarsource.com/java/RSPEC-1481)
* [Sonar-1172: Unused method parameters should be removed](https://rules.sonarsource.com/java/RSPEC-1172) Sonar-VJ版只对private方法的无用参数告警。


----  

**Rule 17.【推荐】final关键字与性能无关，仅用于下列不可修改的场景**

1） 定义类及方法时，类不可继承，方法不可覆写；

2） 定义基本类型的函数参数和变量，不可重新赋值；

3） 定义对象型的函数参数和变量，仅表示变量所指向的对象不可修改，而对象自身的属性是可以修改的。

----

**Rule 18.【推荐】得墨忒耳法则，不要和陌生人说话**

以下调用，一是导致了对A对象的内部结构(B,C)的紧耦合，二是连串的调用很容易产生NPE，因此链式调用尽量不要过长。

```java
obj.getA().getB().getC().hello();
```

----


