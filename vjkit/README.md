# 1. Overview

唯品会Java开发基础类库，综合各门各派众多开源类库的精华而成， 让开发人员避免底层代码的重复开发，默认就拥有最佳实践，尤其在性能的方面。


综合众多开源类库的精华而成， 让开发人员避免底层代码的重复开发，默认就拥有最佳实践，尤其在性能的方面。

针对“基础，文本，数字，日期，文件，集合，并发，反射”这些开发人员的日常，VJKit做了两件事情：

一是对[Guava](https://github.com/google/guava) 与[Common Lang](https://github.com/apache/commons-lang)中最常用的API的提炼归类，避免了大家直面茫茫多的API(但有些工具类如Guava Cache还是建议直接使用，详见[直用三方工具类](docs/direct_3rd.md) )

二是对各门各派的精华的借鉴移植：比如一些大项目的附送基础库： [Netty](https://github.com/netty/netty/)，[ElasticSearch](https://github.com/elastic/elasticsearch)， 一些专业的基础库 ： [Jodd](https://github.com/oblac/jodd/), [commons-io](https://github.com/apache/commons-io), [commons-collections](https://github.com/apache/commons-collections)； 一些大厂的基础库：[Facebook JCommon](https://github.com/facebook/jcommon)，[twitter commons](https://github.com/twitter/commons)


具体使用文档请在IDE中阅读JavaDoc，以及对应的单元测试写法。


# 2. Usage

Maven : 
```
<dependency>
	<groupId>com.vip.vjtools</groupId>
	<artifactId>vjkit</artifactId>
	<version>1.0.8</version>
</dependency>
```

Download: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.vip.vjtools/vjkit/badge.svg)](http://search.maven.org/#search|gav|1|g:"com.vip.vjtools"%20AND%20a:"vjkit")


# 3. Dependency

要求JDK 7.0及以上版本。

| Project | Version | Optional|
|--- | --- | --- |
|[Guava](https://github.com/google/guava) | 20.0 ||
|[Apache Common Lang](https://github.com/apache/commons-lang) | 3.7 ||
|[Slf4j](https://www.slf4j.org) | 1.7.25 ||
|[Dozer](http://dozermapper.github.io/) | 5.5.1 |Optional for BeanMapper，[选型](https://github.com/vipshop/vjtools/blob/master/vjkit/src/main/java/com/vip/vjtools/vjkit/reflect/BeanMapper.java#L11) |

如果使用Optional的依赖，请参考pom文件在业务项目自行引入

# 4. Sonar Status

[https://sonarcloud.io/dashboard?id=com.vip.vjtools:vjkit](https://sonarcloud.io/dashboard?id=com.vip.vjtools:vjkit)

(sonarcloud use "Sonar Way")



