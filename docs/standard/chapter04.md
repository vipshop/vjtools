# (四) 方法设计

**Rule 1. 【推荐】方法的长度度量**
    
方法尽量不要超过100行，或其他团队共同商定的行数。

另外，方法长度超过8000个字节码时，将不会被JIT编译成二进制码。


* [Sonar-107: Methods should not have too many lines](https://rules.sonarsource.com/java/RSPEC-107)，默认值改为100
* Facebook-Contrib:Performance - This method is too long to be compiled by the JIT

----

**Rule 2. 【推荐】方法的语句在同一个抽象层级上**
    
反例：一个方法里，前20行代码在进行很复杂的基本价格计算，然后调用一个折扣计算函数，再调用一个赠品计算函数。

此时可将前20行也封装成一个价格计算函数，使整个方法在同一抽象层级上。

----

**Rule 3. 【推荐】为了帮助阅读及方法内联，将小概率发生的异常处理及其他极小概率进入的代码路径，封装成独立的方法**

```java
if(seldomHappenCase) {
  hanldMethod();
}

try {
  ...
} catch(SeldomHappenException e) {
  handleException();
}
```

----  
  
**Rule 4. 【推荐】尽量减少重复的代码，抽取方法**

超过5行以上重复的代码，都可以考虑抽取公用的方法。

----  
  
**Rule 5. 【推荐】方法参数最好不超过3个，最多不超过7个**

1）如果多个参数同属于一个对象，直接传递对象。

例外: 你不希望依赖整个对象，传播了类之间的依赖性。 


2）将多个参数合并为一个新创建的逻辑对象。

例外: 多个参数之间毫无逻辑关联。    


3）将函数拆分成多个函数，让每个函数所需的参数减少。
   
* [Sonar-107: Methods should not have too many parameters](https://rules.sonarsource.com/java/RSPEC-107)

----  

**Rule 6.【推荐】下列情形，需要进行参数校验**

1） 调用频次低的方法。 

2） 执行时间开销很大的方法。此情形中，参数校验时间几乎可以忽略不计，但如果因为参数错误导致中间执行回退，或者错误，代价更大。    

3） 需要极高稳定性和可用性的方法。     

4） 对外提供的开放接口，不管是RPC/HTTP/公共类库的API接口。    

如果使用Apache Validate 或 Guava Precondition进行校验，并附加错误提示信息时，注意不要每次校验都做一次字符串拼接。

```java
//WRONG
Validate.isTrue(length > 2, "length is "+keys.length+", less than 2", length);
//RIGHT
Validate.isTrue(length > 2, "length is %d, less than 2", length);
```

----  

**Rule 7.【推荐】下列情形，不需要进行参数校验**

1） 极有可能被循环调用的方法。    


2） 底层调用频度比较高的方法。毕竟是像纯净水过滤的最后一道，参数错误不太可能到底层才会暴露问题。

比如，一般DAO层与Service层都在同一个应用中，所以DAO层的参数校验，可以省略。


3） 被声明成private，或其他只会被自己代码所调用的方法，如果能够确定在调用方已经做过检查，或者肯定不会有问题则可省略。    
    
即使忽略检查，也尽量在方法说明里注明参数的要求，比如vjkit中的@NotNull，@Nullable标识。
    
----  

**Rule 8.【推荐】禁用assert做参数校验**

assert断言仅用于测试环境调试，无需在生产环境时进行的校验。因为它需要增加-ea启动参数才会被执行。而且校验失败会抛出一个AssertionError(属于Error，需要捕获Throwable）

因此在生产环境进行的校验，需要使用Apache Commons Lang的Validate或Guava的Precondition。

----  

**Rule 9.【推荐】返回值可以为Null，可以考虑使用JDK8的Optional类**

不强制返回空集合，或者空对象。但需要添加注释充分说明什么情况下会返回null值。 

本手册明确`防止NPE是调用者的责任`。即使被调用方法返回空集合或者空对象，对调用者来说，也并非高枕无忧，必须考虑到远程调用失败、序列化失败、运行时异常等场景返回null的情况。

JDK8的Optional类的使用这里不展开。

----  

**Rule 10.【推荐】返回值可以为内部数组和集合**

如果觉得被外部修改的可能性不大，或没有影响时，不强制在返回前包裹成Immutable集合，或进行数组克隆。

----  

**Rule 11.【推荐】不能使用有继承关系的参数类型来重载方法**

因为方法重载的参数类型是根据编译时表面类型匹配的，不根据运行时的实际类型匹配。

```java
class A {
  void hello(List list);
  void hello(ArrayList arrayList);
}

List arrayList = new ArrayList();

// 下句调用的是hello(List list)，因为arrayList的定义类型是List
a.hello(arrayList);  
```

----  

**Rule 12.【强制】正被外部调用的接口，不允许修改方法签名，避免对接口的调用方产生影响**
     
只能新增新接口，并对已过时接口加@Deprecated注解，并清晰地说明新接口是什么。

----  

**Rule 13.【推荐】不使用`@Deprecated`的类或方法**

接口提供方既然明确是过时接口并提供新接口，那么作为调用方来说，有义务去考证过时方法的新实现是什么。

比如java.net.URLDecoder 中的方法decode(String encodeStr) 这个方法已经过时，应该使用双参数decode(String source, String encode)。

---- 

**Rule 14.【推荐】不使用不稳定方法，如com.sun.\*包下的类，底层类库中internal包下的类**
    
`com.sun.*`，`sun.*`包下的类，或者底层类库中名称为internal的包下的类，都是不对外暴露的，可随时被改变的不稳定类。

* [Sonar-1191: Classes from "sun.*" packages should not be used](https://rules.sonarsource.com/java/RSPEC-1191)

----

