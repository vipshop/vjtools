## 1. 概述

唯品会Java开发基础类库，提升开发的便捷性，同时封装高性能最佳实践的类库。

## 2. Usage

Maven:

```
<dependency>
	<groupId>com.vip.vjtools</groupId>
	<artifactId>vjkit</artifactId>
	<version>1.0.0</version>
</dependency>
```

## 3. Dependency

| Project | Version | Optional|
|--- | --- | --- |
|[guava](https://github.com/google/guava) | 20.0 ||
|[Apache Common Lang](https://github.com/apache/commons-lang) | 3.7 ||
|[Slf4j](https://www.slf4j.org) | 1.7.25 ||
|[Dozer](http://dozermapper.github.io/) | 5.5.1 |Optional for BeanMapper |

如果使用Optional的依赖，请参考pom文件在业务项目自行引入

## 4. Document

### 4.1 概览

具体信息请阅读JavaDoc，以及对应的单元测试写法。

* 基础
* 文本
* 数字
* 日期
* 集合
* 文件
* 并发
* 反射
* 其他


VJKit将Guava与Common Lang中有用的API作了提炼，避免茫茫的API。但有些著名工具类如Guava Cache，StringUtils，则建议直接使用，见[著名三方工具类](docs/famous3rd.md)

### 4.2 参考项目

|||
|--- | --- |
|开源项目自带|[Netty](https://github.com/netty/netty/)，[ElasticSearch](https://github.com/elastic/elasticsearch)|
| 专门Utils |[Jodd](https://github.com/oblac/jodd/), [commons-io](https://github.com/apache/commons-io), [commons-collections](https://github.com/apache/commons-collections)|
| 大厂的开源Utils|[Facebook JCommon](https://github.com/facebook/jcommon)，[twitter commons](https://github.com/twitter/commons)


