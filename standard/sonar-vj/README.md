# Sonar VJ 规则

## 概述

我们使用[Sonar](https://www.sonarqube.org/)代码检查工具来辅助[《唯品会Java开发手册》](https://vipshop.github.io/vjtools/#/standard/)的落地。选择的原因是它同时提供了服务端的公共报表，及IDE端的Sonar Lint插件，而且对比PMD与FindBugs，规则也相对容易定制。

因为Sonar有些规则存在误报的情况，我们在力所能及的范围内对规则的实现进行了修改，以符合我们的规范。

## 实现方式

根据[Writing Custom Java Rules 101](https://docs.sonarqube.org/display/PLUG/Writing+Custom+Java+Rules+101)，从[Sample Project](https://github.com/SonarSource/sonar-custom-rules-examples/tree/master/java-custom-rules)复制创建，对[Sonar Java](https://github.com/SonarSource/sonar-java/tree/master/java-checks/src/main/java/org/sonar/java/checks)规则进行修改。


## 使用方式

官方的Sonar Java Plugin在不断更新，以下修改未必对应其最新版，仅作为修改示例供大家参考(修改部分在代码中以//VJ 标注)。 

如果需要直接使用，编译后扔进sonar的lib目录，重启sonar后取消对原规则的检查，改为使用这些编号一样，带标题带VJ字样的规则即可。

## 修改规则列表

| 编号 | 等级 | 规则描述 | 修改 |
| -------- | -------- |-------- | -------- |
| 1068| Major | Unused "private" fields should be removed | 忽略由Lombok自动生成的getter/setter的类，私有变量不算无用变量 |
| 1172| Major | Unused method parameters should be removed | 只检查private方法是否有无用参数, 忽略其他公共方法 |
| 1166| Major | Exception handlers should preserve the original exceptions | 忽略异常变量名含ignore字样的检查，可以不进行处理，如catch(Exception ignore) |
| 121| Major | Control structures should use curly braces | if语句忽略一般由IDE生成的equals()方法，以及if(condition) return;的单行模式|
| 1068| Major |Limited dependence should be placed on operator precedence rules in expressions| 忽略三目运算符，不需要加括号来清晰优先级 |
| 115| Minor| Constant names should comply with a naming convention| 忽略对枚举成员的全大写检查 |
| 1312| Minor| IP addresses should not be hardcoded | 忽略对"127.0.0.1"的检查 |
| 1291| Info | Track uses of "NOSONAR" comments| 忽略行内含 Exception/Throwable, System.in/System.err的//NOSOANR 检查  |
