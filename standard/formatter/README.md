# 公司通用代码格式化模板

定制原因详见[第二章:格式规约](/standard/docs/chapter02.md)，同时参考了一些Intellij IDEA默认模板的部分设置。

将下列profile下载并导入IDE即可，导入后Profile名称为`vipshop2.0`：

* [Eclipse Code Formatter Profile](http://gitlab.tools.vipshop.com/venus-framework/vjtools/raw/master/standard/formatter/vipshop-code-conventions.xml)
* [Intellij Code Formatter Profile](http://gitlab.tools.vipshop.com/venus-framework/vjtools/raw/master/standard/formatter/vipshop-code-conventions-idea.xml)

因为Intellij导入Eclipse Profile存在问题，因此同时提供了两者的Profile。

## 1. 与 Eclipse 4.6 的`Eclipse [build-in]`模板的区别

* 不格式化JavaDoc
* 注释行宽从80改为120
* 打开format on/off标志
* 参考Intellij IDEA默认模板的修改(见后)


注意：Eclipse后来的build-in模板，代码行宽已经默认120。

## 2. 与原Venus代码格式化模板的区别

* 不格式化JavaDoc
* 不格式化文件头注释(即License声明部分)
* 参考Intellij IDEA默认模板的修改(见后)

## 3. 与IDEA默认模板的区别

详见 [idea和eclipse默认模板的区别](http://wiki.corp.vipshop.com/pages/viewpage.action?pageId=424282717)

本模板参考了Intellij IDEA默认模板中如下部分： 

* 简单的if语句，如果没有括号，则格式化成同一行。(勾选Control Statement->if else->Keep  simple 'if'  on one line)

```java
if (2 < 3) return;
```

当然，我们还是建议用括号，此处格式化成一行只是兜底的保护。

* 主动输入的空行，最多可保留两行 (Blank Lines->Existing blank lines -> Number of empty lines to preserve 从1 改为 2)

* switch 和 case 之间缩进(勾选Indentation-> Indent->Statements within switch body)

```java
switch (a) {
  case 0:
    doCase0();
    break;
  default:
    doDefault();    
}
```

* 数组构造时不要那么多空格(取消White Space->Arrays->Array Initializers->before opening brace,after opening brace,before closing brace)

```java
int[] a  = new int[]{1, 2, 3}; 
```