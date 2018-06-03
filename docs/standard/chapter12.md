# (十二) 其他规约

**Rule 1. 【参考】尽量不要让魔法值（即未经定义的数字或字符串常量）直接出现在代码中** 

```java
 //WRONG
 String key = "Id#taobao_"+tradeId;
 cache.put(key, value);
```
例外：-1,0,1,2,3 不认为是魔法数

* [Sonar-109: Magic numbers should not be used](https://rules.sonarsource.com/java/RSPEC-109) 但现实中所谓魔法数还是太多，该规则不能被真正执行。

----

**Rule 2. 【推荐】时间获取的原则**

1）获取当前毫秒数System.currentTimeMillis() 而不是new Date().getTime()，后者的消耗要大得多。


2）如果要获得更精确的，且不受NTP时间调整影响的流逝时间，使用System.nanoTime()获得机器从启动到现在流逝的纳秒数。


3）如果希望在测试用例中控制当前时间的值，则使用vjkit的Clock类封装，在测试和生产环境中使用不同的实现。

----  

**Rule 3. 【推荐】变量声明尽量靠近使用的分支** 

不要在一个代码块的开头把局部变量一次性都声明了(这是c语言的做法)，而是在第一次需要使用它时才声明。
    
否则如果方法已经退出或进入其他分支，就白白初始化了变量。

```java
//WRONG
Foo foo = new Foo();

if(ok){
	return;
}

foo.bar();
```

----  

**Rule 4. 【推荐】不要像C那样一行里做多件事情** 

```java
 //WRONG
fooBar.fChar = barFoo.lchar = 'c'; 
argv++; argc--;       
int level, size;
```

* [Sonar-1659: Multiple variables should not be declared on the same line](https://rules.sonarsource.com/java/RSPEC-1659)

----  

**Rule 5. 【推荐】不要为了性能而使用JNI本地方法**

Java在JIT后并不比C代码慢，JNI方法因为要反复跨越JNI与Java的边界反而有额外的性能损耗。
    
因此JNI方法仅建议用于调用"JDK所没有包括的, 对特定操作系统的系统调用"

----  

**Rule 6. 【推荐】正确使用反射，减少性能损耗**

获取Method/Field对象的性能消耗较大, 而如果对Method与Field对象进行缓存再反复调用，则并不会比直接调用类的方法与成员变量慢（前15次使用NativeAccessor，第15次后会生成GeneratedAccessorXXX，bytecode为直接调用实际方法）
    
```java
//用于对同一个方法多次调用
private Method method = ....

public void foo(){
  method.invoke(obj, args);
}

//用于仅会对同一个方法单次调用
ReflectionUtils.invoke(obj, methodName, args);
```

----  

**Rule 7.【推荐】可降低优先级的常见代码检查规则**

1. 接口内容的定义中，去除所有modifier，如public等。 (多个public也没啥，反正大家都看惯了)

2. 工具类，定义private构造函数使其不能被实例化。（命名清晰的工具类，也没人会去实例化它，对静态方法通过类来访问也能避免实例化）

----


