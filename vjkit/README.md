## 1. Overview

唯品会Java开发基础类库，综合众多开源类库精华， 让业务人员避免底层代码的重复开发，默认就拥有最佳实践，尤其在性能的方面。


VJKit中的内容，一是源于对JDK，Guava与Common Lang中API的提炼，避免了茫茫多的API (但有些工具类如Guava Cache则建议直接使用，详见[著名三方工具类](docs/famous3rd.md) )

二是对各门各派的精华的借鉴移植：比如一些著名项目的附带基础库： [Netty](https://github.com/netty/netty/)，[ElasticSearch](https://github.com/elastic/elasticsearch)； 专门的基础库 ： [Jodd](https://github.com/oblac/jodd/), [commons-io](https://github.com/apache/commons-io), [commons-collections](https://github.com/apache/commons-collections)； 大厂的基础库：[Facebook JCommon](https://github.com/facebook/jcommon)，[twitter commons](https://github.com/twitter/commons)


整合之后分类如下：基础 ， 文本 ， 数字 ， 日期 ， 集合 ， 文件 ， 并发 ， 反射 ， 其他 ， 具体使用文档请阅读JavaDoc，以及对应的单元测试写法。



## 2. Usage

Maven:

```
<dependency>
	<groupId>com.vip.vjtools</groupId>
	<artifactId>vjkit</artifactId>
	<version>1.0.0</version>
</dependency>
```

[Maven Central 下载](http://repo1.maven.org/maven2/com/vip/vjtools/vjkit/1.0.0/)

## 3. Dependency

要求JDK 7.0及以上版本。

| Project | Version | Optional|
|--- | --- | --- |
|[guava](https://github.com/google/guava) | 20.0 ||
|[Apache Common Lang](https://github.com/apache/commons-lang) | 3.7 ||
|[Slf4j](https://www.slf4j.org) | 1.7.25 ||
|[Dozer](http://dozermapper.github.io/) | 5.5.1 |Optional for BeanMapper |

如果使用Optional的依赖，请参考pom文件在业务项目自行引入



