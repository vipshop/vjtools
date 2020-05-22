# 1.简介
vjmask是唯品会的日志脱敏组件，在业务中广泛使用。基于性能和通用性考虑，采用了现在的方案。让使用方用最少的配置和代码，就可以轻松实现敏感信息过滤。

# 2.使用
## 2.1 依赖

```
<dependency>
    <groupId>com.vip.vjtools</groupId>
    <artifactId>vjkit</artifactId>
    <version>${version}</version>
</dependency>
```

## 2.2 最佳实践
脱敏组件支持对单个字符串进行脱敏，也支持toJSON和toString的序列化脱敏

```
// 单个字符串，按中文姓名规则脱敏
logger.info("some sensitive info:{}",DataMask.maskByType(name,SensitiveType.Name));
//单个字符串，按默认方式脱敏，结果为 "t***";
logger.info("some sensitive info:{}",DataMask.mask("test")); 
 
//对象json序列化脱敏
logger.info("some sensitive object:{}",DataMask.toJSONString(obj));
//对象toString序列化脱敏
logger.info("some sensitive object:{}",DataMask.toString(obj));
```

对类的序列化脱敏，先要对相关敏感字段标记 @Sensitive，支持字符串类型的字段，包括String、String[]和Collection<String>. 嵌套类的敏感字段也是可以识别的。

```
public class User{
  @Sensitive(type = SensitiveType.Phone)  type参考2.3
  private String phone;
}
```

## 2.3 脱敏规则
我们已经定义实现了常用的脱敏类型，可以直接使用

敏感信息 | 脱敏要求 | 样例 | SensitiveType
---|---|---|---
中文姓名 | 三个字及以下，只显示最后一个字;三个字以上，显示最后两个字 | *明，****小明 | Name
手机号/固定电话 | 只显示前三后三 | 138*****111 | Phone
身份证号 | 显示前五个和后二个字符 | 44010************58 | IDCard
银行卡号 | 显示前四个和后二个字符 | 6228************89 | BankCard
地址 | 保留前9个字符 | 广东省广州市荔湾区****** | Address
电子邮箱 | 只显示前一后一及@和后面的内容 | a***b@abc.com | Email
验证码 | 只显示前一后一 | a**b | Captcha
护照/军官号 | 只显示前二后二 | EI****64 | Passport
账号 | 只显示前一后一 | a****b | Account
密码 | 不显示任意字符 | ********* | Password
散列 | sha1(source+salt) ，可以通过DataMask.setSalt设置 | 6b76e070c5b5d1b889295506faa8b98e97da7e87 | Hash


# 3.详细介绍
## 3.1 Annotation标注
要使用序列化脱敏，先要对敏感字段标注@Sensitive，注意，只对字符串相关的类型字段会生效。

```
//根据类型来标注
@Sensitive(type = SensitiveType.Name)
private String name;
 
//也可以自定义掩码规则
@Sensitive(keepChars = 2) //首尾保留2个字符串，如果keepChars = {1,3} 表示头部保留1个字符，尾部保留3个字符
private String[] phone;
 
// 散列的方式
@Sensitive(type = SensitiveType.Hash)
private String hash;
 
//默认的方式,只保留第一个字符串
@Sensitive
private List<String> account;
```

## 3.2 映射配置
如果你不想对一个个类字段标注@Sensitive,也可以在resource 目录下新建一个data_mask。properties,添加敏感字段映射

```
#SensitiveType=字段名称
Name=nickName
```
那么nickName字段即使没有标注@Sensitive ，在序列化的时候，也会自动脱敏，按照SentiveType.Name的方式脱敏。 

==注意，标注@Sensitive优先级高于文件配置==

组件中已经默认配置映射了以下配置，sys_data_mask.properties

```
Name=chineseName,userName
Phone=phone,phoneNum,mobile,tel,telephone
IDCard=IDCard,IdNo
BankCard=bankCard
Address=address,addr
Email=mail,email
Captcha=captcha
Passport=passport
Account=account
Password=password,passwd
```

# 4.Benchmark
我们使用JMH基准测试，测试了2个使用场景。
## 4.1 场景1
通用场景，场景1包括了10个左右的Sensitive字段。toJSONString() 平均耗时 0.003ms，toString() 平均耗时0.005ms。


```
Benchmark                      Mode    Score    Error  Units
DataMaskTest.testJson(不脱敏)   avgt    0.001 ±  0.001  ms/op
DataMaskTest.testMaskJson      avgt    0.003 ±  0.002  ms/op
DataMaskTest.testMaskToString  avgt    0.005 ±  0.003  ms/op
DataMaskTest.testToString      avgt    ≈ 10⁻⁴          ms/op
```

## 4.2 场景2
极限场景，场景2测试超大的类序列化，有160多个字段需要脱敏，3层嵌套。toJSONString() 平均耗时 0.064ms，toString() 平均耗时0.098ms。
```
Benchmark                      Mode  Score    Error  Units
DataMaskTest.testJson          avgt  0.043 ±  0.025  ms/op
DataMaskTest.testMaskJson      avgt  0.064 ±  0.002  ms/op
DataMaskTest.testMaskToString  avgt  0.098 ±  0.095  ms/op
DataMaskTest.testToString      avgt  ≈ 10⁻⁴          ms/op
```

## 4.3 结论
脱敏处理的性能和脱敏字段的数量相关。

a.如果需要脱敏的字段不多，时间在0.001ms~0.009ms范围内，对于业务来说，不会造成太多额外开销，在可控范围内。

b.如果需要脱敏的字段特别多，建议要性能测试评估

c.toString() 性能比toJson() 要慢，建议是优先使用toJson()
