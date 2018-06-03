# (八) 集合处理

**Rule 1. 【推荐】底层数据结构是数组的集合，指定集合初始大小**

底层数据结构为数组的集合包括 ArrayList，HashMap，HashSet，ArrayDequeue等。

数组有大小限制，当超过容量时，需要进行复制式扩容，新申请一个是原来容量150% or 200%的数组，将原来的内容复制过去，同时浪费了内存与性能。HashMap/HashSet的扩容，还需要所有键值对重新落位，消耗更大。


默认构造函数使用默认的数组大小，比如ArrayList默认大小为10，HashMap为16。因此建议使用ArrayList(int initialCapacity)等构造函数，明确初始化大小。
    

HashMap/HashSet的初始值还要考虑加载因子:

为了降低哈希冲突的概率(Key的哈希值按数组大小取模后，如果落在同一个数组下标上，将组成一条需要遍历的Entry链)，默认当HashMap中的键值对达到数组大小的75%时，即会触发扩容。因此，如果预估容量是100，即需要设定`100/0.75＝134`的数组大小。vjkit的MapUtil的Map创建函数封装了该计算。

如果希望加快Key查找的时间，还可以进一步降低加载因子，加大初始大小，以降低哈希冲突的概率。

----  

**Rule 2. 【推荐】尽量使用新式的foreach语法遍历Collection与数组**

foreach是语法糖，遍历集合的实际字节码等价于基于Iterator的循环。
    
foreach代码一来代码简洁，二来有效避免了有多个循环或嵌套循环时，因为不小心的复制粘贴，用错了iterator或循环计数器(i,j)的情况。

----  

**Rule 3. 【强制】不要在foreach循环里进行元素的remove/add操作，remove元素可使用Iterator方式**

```java
//WRONG
for (String str : list) {
  if (condition) {
    list.remove(str);
  }
}

//RIGHT
Iterator<String> it = list.iterator();
while (it.hasNext()) {
  String str = it.next();
  if (condition) {
    it.remove();
  }
} 
```

* Facebook-Contrib: Correctness - Method modifies collection element while iterating
* Facebook-Contrib: Correctness - Method deletes collection element while iterating

----  

**Rule 4. 【强制】使用entrySet遍历Map类集合Key/Value，而不是keySet	方式进行遍历**

keySet遍历的方式，增加了N次用key获取value的查询。

* [Sonar-2864:"entrySet()" should be iterated when both the key and value are needed](https://rules.sonarsource.com/java/RSPEC-2864)

----  

**Rule 5. 【强制】当对象用于集合时，下列情况需要重新实现hashCode()和 equals()**

1） 以对象做为Map的KEY时；

2） 将对象存入Set时。 

上述两种情况，都需要使用hashCode和equals比较对象，默认的实现会比较是否同一个对象（对象的引用相等）。

另外，对象放入集合后，会影响hashCode()，equals()结果的属性，将不允许修改。

* [Sonar-2141:Classes that don't define "hashCode()" should not be used in hashes](https://rules.sonarsource.com/java/RSPEC-2141)

----  

**Rule 6. 【强制】高度注意各种Map类集合Key/Value能不能存储null值的情况**

| Map | Key | Value |
| -------- | -------- |-------- |
|HashMap|Nullable | Nullable|
|ConcurrentHashMap| NotNull| NotNull|   
|TreeMap| NotNull| Nullable | 

由于HashMap的干扰，很多人认为ConcurrentHashMap是可以置入null值。同理，Set中的value实际是Map中的key。

----  

**Rule 7. 【强制】长生命周期的集合，里面内容需要及时清理，避免内存泄漏**

长生命周期集合包括下面情况，都要小心处理。

1） 静态属性定义；

2） 长生命周期对象的属性；

3） 保存在ThreadLocal中的集合。
   
如无法保证集合的大小是有限的，使用合适的缓存方案代替直接使用HashMap。
   
另外，如果使用WeakHashMap保存对象，当对象本身失效时，就不会因为它在集合中存在引用而阻止回收。但JDK的WeakHashMap并不支持并发版本，如果需要并发可使用Guava Cache的实现。
  
----

**Rule 8. 【强制】集合如果存在并发修改的场景，需要使用线程安全的版本**

1) 著名的反例，HashMap扩容时，遇到并发修改可能造成100%CPU占用。
   
推荐使用`java.util.concurrent(JUC)`工具包中的并发版集合，如ConcurrentHashMap等，优于使用Collections.synchronizedXXX()系列函数进行同步化封装(等价于在每个方法都加上synchronized关键字)。


例外：ArrayList所对应的CopyOnWriteArrayList，每次更新时都会复制整个数组，只适合于读多写很少的场景。如果频繁写入，可能退化为使用Collections.synchronizedList(list)。


2) 即使线程安全类仍然要注意函数的正确使用。

例如：即使用了ConcurrentHashMap，但直接是用get/put方法，仍然可能会多线程间互相覆盖。

```java
//WRONG
E e = map.get(key);
if (e == null) {
  e = new E();
  map.put(key, e); //仍然能两条线程并发执行put，互相覆盖
}
return e;

//RIGHT 
E e = map.get(key);
if (e == null) {
  e = new E();
  E previous = map.putIfAbsent(key, e);
  if(previous != null) {
    return previous;
  }
}
return e;
```
----

**Rule 9. 【推荐】正确使用集合泛型的通配符**

`List<String>`并不是`List<Object>`的子类，如果希望泛型的集合能向上向下兼容转型，而不仅仅适配唯一类，则需定义通配符，可以按需要extends 和 super的字面意义，也可以遵循`PECS(Producer Extends Consumer Super)`原则:

1) 如果集合要被读取，定义成`<? extends T>`

```java
Class Stack<E>{
  public void pushAll(Iterable<? extends E> src){
    for (E e: src)
      push(e);
  }
}

Stack<Number> stack = new Stack<Number>();
Iterable<Integer> integers = ...;
stack.pushAll(integers);
```


2) 如果集合要被写入，定义成`<? super T>`

```java
Class Stack<E>{
  public void popAll(Collection<? super E> dist){
     while(!isEmpty())
   	   dist.add(pop);   
  }
}

Stack<Number> stack = new Stack<Number>();
Collection<Object> objects = ...;
stack.popAll(objects);
```
----

**Rule 10. 【推荐】`List`, `List<?>` 与 `List<Object>`的选择**

定义成`List`，会被IDE提示需要定义泛型。 如果实在无法确定泛型，就仓促定义成`List<?>`来蒙混过关的话，该list只能读，不能增改。定义成`List<Object>`呢，如规则10所述，`List<String>` 并不是`List<Object>`的子类，除非函数定义使用了通配符。

因此实在无法明确其泛型时，使用`List`也是可以的。

----  

**Rule 11. 【推荐】如果Key只有有限的可选值，先将Key封装成Enum，并使用EnumMap**

EnumMap，以Enum为Key的Map，内部存储结构为`Object[enum.size]`，访问时以`value = Object[enum.ordinal()]`获取值，同时具备HashMap的清晰结构与数组的性能。

```java
public enum COLOR {
  RED, GREEN, BLUE, ORANGE;
}
  
EnumMap<COLOR, String> moodMap = new EnumMap<COLOR, String> (COLOR.class);
```

* [Sonar-1640: Maps with keys that are enum values should be replaced with EnumMap](https://rules.sonarsource.com/java/RSPEC-1640)

----  

**Rule 12. 【推荐】Array 与 List互转的正确写法**

```java
// list -> array，构造数组时不需要设定大小
String[] array = (String[])list.toArray(); //WRONG;
String[] array = list.toArray(new String[0]); //RIGHT
String[] array = list.toArray(new String[list.size()]); //RIGHT，但list.size()可用0代替。


// array -> list
List list = Arrays.asList(array); //WRONG
List list = new ArrayList(array); //RIGHT
```
Arrays.asList(array)，如果array是原始类型数组如int[]，会把整个array当作List的一个元素，String[] 或 Foo[]则无此问题，安全起见统一不使用。

* Facebook-Contrib: Correctness - Impossible downcast of toArray() result
* Facebook-Contrib: Correctness - Method calls Array.asList on an array of primitive values

----



