# (二) 格式规约

**Rule 1. 【强制】使用项目组统一的代码格式模板，基于IDE自动的格式化**
    
1）IDE的默认代码格式模板，能简化绝大部分关于格式规范(如空格，括号)的描述。

2）统一的模板，并在接手旧项目先进行一次全面格式化，可以避免， 不同开发者之间，因为格式不统一产生代码合并冲突，或者代码变更日志中因为格式不同引起的变更，掩盖了真正的逻辑变更。

3）设定项目组统一的行宽，建议120。

4）设定项目组统一的缩进方式(Tab或二空格，四空格均可)，基于IDE自动转换。


* [VIP代码格式化模板](https://github.com/vipshop/vjtools/tree/master/standard/formatter)
    
----

**Rule 2. 【强制】IDE的text file encoding设置为UTF-8; IDE中文件的换行符使用Unix格式，不要使用Windows格式**

----

**Rule 3. 【推荐】 用小括号来限定运算优先级**

我们没有理由假设读者能记住整个Java运算符优先级表。除非作者和Reviewer都认为去掉小括号也不会使代码被误解，甚至更易于阅读。 

```java
if ((a == b) && (c == d))
```

* [Sonar-1068:Limited dependence should be placed on operator precedence rules in expressions](https://www.sonarsource.com/products/codeanalyzers/sonarjava/rules.html#RSPEC-1068)，我们修改了三目运算符 `foo!=null?foo:""` 不需要加括号。

----

**Rule 4. 【推荐】类内方法定义的顺序，不要“总是在类的最后添加新方法”**
    
一个类就是一篇文章，想象一个阅读者的存在，合理安排方法的布局。
   
1）顺序依次是：构造函数 > (公有方法>保护方法>私有方法)  > getter/setter方法。
 
如果公有方法可以分成几组，私有方法也紧跟公有方法的分组。


2）当一个类有多个构造方法，或者多个同名的重载方法，这些方法应该放置在一起。其中参数较多的方法在后面。

```java
public Foo(int a) {...}
public Foo(int a, String b) {...}

public void foo(int a) {...}
public void foo(int a, String b) {...}
```


3）作为调用者的方法，尽量放在被调用的方法前面。

```java
public void foo() {
	bar();
}

public void bar() {...}
```

----

**Rule 5. 【推荐】通过空行进行逻辑分段**

一段代码也是一段文章，需要合理的分段而不是一口气读到尾。

不同组的变量之间，不同业务逻辑的代码行之间，插入一个空行，起逻辑分段的作用。  
    
而联系紧密的变量之间、语句之间，则尽量不要插入空行。
    
```java
int width; 
int height; 

String name;
```

----

**Rule 6.【推荐】避免IDE格式化**

对于一些特殊场景（如使用大量的字符串拼接成一段文字，或者想把大量的枚举值排成一列），为了避免IDE自动格式化，土办法是把注释符号//加在每一行的末尾，但这有视觉的干扰，可以使用@formatter:off和@formatter:on来包装这段代码，让IDE跳过它。

``` java
// @formatter:off
...
// @formatter:on
```
----

