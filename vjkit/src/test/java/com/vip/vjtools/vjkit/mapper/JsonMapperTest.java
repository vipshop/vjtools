package com.vip.vjtools.vjkit.mapper;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 测试Jackson对Object,Map,List,数组,枚举,日期类等的持久化. 更多测试见showcase中的JsonDemo.
 */
public class JsonMapperTest {

	/**
	 * 序列化对象/集合到Json字符串.
	 */
	@Test
	public void toJson() throws Exception {
		// Bean
		TestBean bean = new TestBean("A");
		String beanString = JsonMapper.INSTANCE.toJson(bean);
		System.out.println("Bean:" + beanString);
		assertThat(beanString)
				.isEqualTo("{\"name\":\"A\",\"defaultValue\":\"hello\",\"nullValue\":null,\"emptyValue\":[]}");

		// Map
		Map<String, Object> map = Maps.newLinkedHashMap();
		map.put("name", "A");
		map.put("age", 2);
		String mapString = JsonMapper.INSTANCE.toJson(map);
		System.out.println("Map:" + mapString);
		assertThat(mapString).isEqualTo("{\"name\":\"A\",\"age\":2}");

		// List<String>
		List<String> stringList = Lists.newArrayList("A", "B", "C");
		String listString = JsonMapper.INSTANCE.toJson(stringList);
		System.out.println("String List:" + listString);
		assertThat(listString).isEqualTo("[\"A\",\"B\",\"C\"]");

		// List<Bean>
		List<TestBean> beanList = Lists.newArrayList(new TestBean("A"), new TestBean("B"));
		String beanListString = JsonMapper.INSTANCE.toJson(beanList);
		System.out.println("Bean List:" + beanListString);
		assertThat(beanListString).isEqualTo(
				"[{\"name\":\"A\",\"defaultValue\":\"hello\",\"nullValue\":null,\"emptyValue\":[]},{\"name\":\"B\",\"defaultValue\":\"hello\",\"nullValue\":null,\"emptyValue\":[]}]");

		// Bean[]
		TestBean[] beanArray = new TestBean[] { new TestBean("A"), new TestBean("B") };
		String beanArrayString = JsonMapper.INSTANCE.toJson(beanArray);
		System.out.println("Array List:" + beanArrayString);
		assertThat(beanArrayString).isEqualTo(
				"[{\"name\":\"A\",\"defaultValue\":\"hello\",\"nullValue\":null,\"emptyValue\":[]},{\"name\":\"B\",\"defaultValue\":\"hello\",\"nullValue\":null,\"emptyValue\":[]}]");
	}

	/**
	 * 从Json字符串反序列化对象/集合.
	 */
	@Test
	public void fromJson() throws Exception {
		// Bean
		String beanString = "{\"name\":\"A\"}";
		TestBean bean = JsonMapper.INSTANCE.fromJson(beanString, TestBean.class);
		System.out.println("Bean:" + bean);

		// Map
		String mapString = "{\"name\":\"A\",\"age\":2}";
		Map<String, Object> map = JsonMapper.INSTANCE.fromJson(mapString, HashMap.class);
		System.out.println("Map:");
		for (Entry<String, Object> entry : map.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}

		// List<String>
		String listString = "[\"A\",\"B\",\"C\"]";
		List<String> stringList = JsonMapper.INSTANCE.fromJson(listString, List.class);
		System.out.println("String List:");
		for (String element : stringList) {
			System.out.println(element);
		}

		// List<Bean>
		String beanListString = "[{\"name\":\"A\"},{\"name\":\"B\"}]";
		List<TestBean> beanList = JsonMapper.INSTANCE.fromJson(beanListString,
				JsonMapper.INSTANCE.buildCollectionType(List.class, TestBean.class));
		System.out.println("Bean List:");
		for (TestBean element : beanList) {
			System.out.println(element);
		}
	}

	/**
	 * 测试传入空对象,空字符串,Empty的集合,"null"字符串的结果.
	 */
	@Test
	public void nullAndEmpty() {
		// toJson测试 //

		// Null Bean
		TestBean nullBean = null;
		String nullBeanString = JsonMapper.INSTANCE.toJson(nullBean);
		assertThat(nullBeanString).isEqualTo("null");


		// Empty List
		List<String> emptyList = Lists.newArrayList();
		String emptyListString = JsonMapper.INSTANCE.toJson(emptyList);
		assertThat(emptyListString).isEqualTo("[]");

		// fromJson测试 //

		// Null String for Bean
		TestBean nullBeanResult = JsonMapper.INSTANCE.fromJson(null, TestBean.class);
		assertThat(nullBeanResult).isNull();

		nullBeanResult = JsonMapper.INSTANCE.fromJson("null", TestBean.class);
		assertThat(nullBeanResult).isNull();

		nullBeanResult = JsonMapper.INSTANCE.fromJson("", TestBean.class);
		assertThat(nullBeanResult).isNull();

		nullBeanResult = JsonMapper.INSTANCE.fromJson("{}", TestBean.class);
		assertThat(nullBeanResult).isNotNull();
		assertThat(nullBeanResult.getDefaultValue()).isEqualTo("hello");


		// Null/Empty String for List
		List nullListResult = JsonMapper.INSTANCE.fromJson(null, List.class);
		assertThat(nullListResult).isNull();

		nullListResult = JsonMapper.INSTANCE.fromJson("null", List.class);
		assertThat(nullListResult).isNull();

		nullListResult = JsonMapper.INSTANCE.fromJson("[]", List.class);
		assertThat(nullListResult).isEmpty();
	}

	/**
	 * 测试三种不同的Mapper.
	 */
	@Test
	public void threeTypeMappers() {
		// 打印全部属性
		JsonMapper normalBinder = JsonMapper.defaultMapper();
		TestBean bean = new TestBean("A");
		assertThat(normalBinder.toJson(bean))
				.isEqualTo("{\"name\":\"A\",\"defaultValue\":\"hello\",\"nullValue\":null,\"emptyValue\":[]}");

		// 不打印nullValue属性
		JsonMapper nonNullMapper = JsonMapper.nonNullMapper();
		assertThat(nonNullMapper.toJson(bean))
				.isEqualTo("{\"name\":\"A\",\"defaultValue\":\"hello\",\"emptyValue\":[]}");

		// 不打印nullValue与empty的属性
		JsonMapper nonEmptyMapper = JsonMapper.nonEmptyMapper();
		assertThat(nonEmptyMapper.toJson(bean)).isEqualTo("{\"name\":\"A\",\"defaultValue\":\"hello\"}");

		TestBean nonEmptyBean = nonEmptyMapper.fromJson("{\"name\":\"A\",\"defaultValue\":\"hello\"}", TestBean.class);
		assertThat(nonEmptyBean.getEmptyValue()).isEmpty();
	}

	@Test
	public void jsonp() {
		TestBean bean = new TestBean("A");
		String jsonp = JsonMapper.nonEmptyMapper().toJsonP("haha", bean);
		assertThat(jsonp).isEqualTo("haha({\"name\":\"A\",\"defaultValue\":\"hello\"})");
	}

	@Test
	public void update() {
		TestBean bean = new TestBean("A");
		bean.setDefaultValue("lalala");
		JsonMapper.INSTANCE.update("{\"name\":\"B\"}", bean);
		assertThat(bean.getName()).isEqualTo("B");
		assertThat(bean.getDefaultValue()).isEqualTo("lalala");
	}

	public static class TestBean {

		private String name;
		private String defaultValue = "hello";
		private String nullValue = null;
		private List<String> emptyValue = new ArrayList();

		public TestBean() {
		}

		public TestBean(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		public String getNullValue() {
			return nullValue;
		}

		public void setNullValue(String nullValue) {
			this.nullValue = nullValue;
		}

		public List<String> getEmptyValue() {
			return emptyValue;
		}

		public void setEmptyValue(List<String> emptyValue) {
			this.emptyValue = emptyValue;
		}

		@Override
		public String toString() {
			return "TestBean [name=" + name + ", defaultValue=" + defaultValue + ", nullValue=" + nullValue
					+ ", emptyValue=" + emptyValue + "]";
		}
	}

}