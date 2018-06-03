# (六) 控制语句

**Rule 1. 【强制】if, else, for, do, while语句必须使用大括号，即使只有单条语句**

曾经试过合并代码时，因为没加括号，单条语句合并成两条语句后，仍然认为只有单条语句，另一条语句在循环外执行。

其他增加调试语句等情况也经常引起同样错误。
    
可在IDE的Save Action中配置自动添加。

```java
if (a == b) {
  ...
}
```

例外：一般由IDE生成的equals()函数

* [Sonar-121: Control structures should use curly braces](https://rules.sonarsource.com/java/RSPEC-121) Sonar-VJ版豁免了equals()函数

----

**Rule 2.【推荐】少用if-else方式，多用哨兵语句式以减少嵌套层次**

```java
if (condition) {
  ...
  return obj;
}

// 接着写else的业务逻辑代码;
```

* Facebook-Contrib: Style - Method buries logic to the right (indented) more than it needs to be

----

**Rule 3.【推荐】限定方法的嵌套层次**

所有if/else/for/while/try的嵌套，当层次过多时，将引起巨大的阅读障碍，因此一般推荐嵌套层次不超过4。  

通过抽取方法，或哨兵语句（见Rule 2）来减少嵌套。

```java
public void applyDriverLicense() {
  if (isTooYoung()) {
    System.out.println("You are too young to apply driver license.");
    return;
  }
    
  if (isTooOld()) {
    System.out.println("You are too old to apply driver license.");
    return;
  }
    
  System.out.println("You've applied the driver license successfully.");
  return;
}
```

* [Sonar-134: Control flow statements "if", "for", "while", "switch" and "try" should not be nested too deeply](https://rules.sonarsource.com/java/RSPEC-134)，增大为4

----

**Rule 4.【推荐】布尔表达式中的布尔运算符(&&,||)的个数不超过4个，将复杂逻辑判断的结果赋值给一个有意义的布尔变量名，以提高可读性**


```java
//WRONG
if ((file.open(fileName, "w") != null) && (...) || (...)|| (...)) {
  ...
}

//RIGHT
boolean existed = (file.open(fileName, "w") != null) && (...) || (...);
if (existed || (...)) {
  ...
}
```

* [Sonar-1067: Expressions should not be too complex](https://rules.sonarsource.com/java/RSPEC-1067)，增大为4

----

**Rule 5.【推荐】简单逻辑，善用三元运算符，减少if-else语句的编写**

```java
s != null ? s : "";
```

----

**Rule 6.【推荐】减少使用取反的逻辑**

不使用取反的逻辑，有利于快速理解。且大部分情况，取反逻辑存在对应的正向逻辑写法。

```java
//WRONG
if (!(x >= 268) { ... }

//RIGHT
if (x < 268) { ... }
```

* [Sonar-1940: Boolean checks should not be inverted](https://rules.sonarsource.com/java/RSPEC-1940)

----

**Rule 7.【推荐】表达式中，能造成短路概率较大的逻辑尽量放前面，使得后面的判断可以免于执行**


```java
if (maybeTrue() || maybeFalse()) { ... }

if (maybeFalse() && maybeTrue()) { ... }
```

----


**Rule 8.【强制】switch的规则**

1）在一个switch块内，每个case要么通过break/return等来终止，要么注释说明程序将继续执行到哪一个case为止；
    
2）在一个switch块内，都必须包含一个default语句并且放在最后，即使它什么代码也没有。

```java
String animal = "tomcat";

switch (animal) {
case "cat":
  System.out.println("It's a cat.");
  break;
case "lion": // 执行到tiger
case "tiger":
  System.out.println("It's a beast.");
  break;
default: 
  // 什么都不做，也要有default
  break;
}
```

* [Sonar: "switch" statements should end with "default" clauses](https://rules.sonarsource.com/java/RSPEC-131)

----

**Rule 9.【推荐】循环体中的语句要考量性能，操作尽量移至循环体外处理**

1）不必要的耗时较大的对象构造；

2）不必要的try-catch（除非出错时需要循环下去）。

----

**Rule 10.【推荐】能用while循环实现的代码，就不用do-while循环**

while语句能在循环开始的时候就看到循环条件，便于帮助理解循环内的代码；

do-while语句要在循环最后才看到循环条件，不利于代码维护，代码逻辑容易出错。

----


