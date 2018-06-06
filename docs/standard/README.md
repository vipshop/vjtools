# 《唯品会Java开发手册》1.0版

## 1. 概述

[《阿里巴巴Java开发手册》](https://github.com/alibaba/p3c)，是首个对外公布的企业级Java开发手册，对整个业界都有重要的意义。

我们结合唯品会的内部经验，参考《Clean Code》、《Effective Java》等重磅资料，增补了一些条目，同时删减了一些相对不那么通用的规则，让规范更精炼易记。

感谢阿里的授权修改。


## 2. 规范正文

1. [命名规约](standard/chapter01.md)
2. [格式规约](standard/chapter02.md)
3. [注释规约](standard/chapter03.md)
4. [方法设计](standard/chapter04.md)
5. [类设计](standard/chapter05.md)
6. [控制语句](standard/chapter06.md)
7. [基本类型](standard/chapter07.md)
8. [集合处理](standard/chapter08.md)
9. [并发处理](standard/chapter09.md)
10. [异常处理](standard/chapter10.md)
11. [日志规约](standard/chapter11.md)
12. [其他设计](standard/chapter12.md)
13. [阿里手册的增补与删减记录](standard/ali.md)


注意： 如需全文pdf版，请运行merge.sh生成，阅读时的章节跳转使用pdf阅读器的左侧书签。

## 3. 规范落地

规则落地主要依靠代码格式化模版与[Sonar代码规则检查](https://www.sonarqube.org/)。

其中Sonar规则不尽如人意的地方，我们进行了定制。

* [Eclipse/Intellij 格式模板](https://github.com/vipshop/vjtools/tree/master/standard/formatter)
* [Sonar 规则修改示例](https://github.com/vipshop/vjtools/tree/master/standard/sonar-vj)

## 4. 参考资料

* [《Clean Code》](https://book.douban.com/subject/4199741/)
* [《Effective Java 2nd》](https://book.douban.com/subject/3360807/)
* [《SEI CERT Oracle Coding Standard for Java》(在线版)](https://www.securecoding.cert.org/confluence/display/java/SEI+CERT+Oracle+Coding+Standard+for+Java)
* [Sonar代码检查规则](https://rules.sonarsource.com/java/)
