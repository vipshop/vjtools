package com.vip.vjtools.vjkit.reflect;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Test;

import com.vip.vjtools.vjkit.base.type.UncheckedException;
import com.vip.vjtools.vjkit.collection.ListUtil;

public class ReflectionUtilTest {

	@Test
	public void getAndSetFieldValue() {
		TestBean bean = new TestBean();
		// 无需getter函数, 直接读取privateField
		assertThat((int) ReflectionUtil.getFieldValue(bean, "privateField")).isEqualTo(1);

		// 先尝试getter函数, 然后直接读取privateField
		assertThat((int) ReflectionUtil.getProperty(bean, "privateField")).isEqualTo(1);

		// 绕过将publicField+1的getter函数,直接读取publicField的原始值
		assertThat((int) ReflectionUtil.getFieldValue(bean, "publicField")).isEqualTo(1);
		// 先尝试getter函数, 成功则补不直接读取publicField
		assertThat((int) ReflectionUtil.getProperty(bean, "publicField")).isEqualTo(2);

		bean = new TestBean();
		// 无需setter函数, 直接设置privateField
		ReflectionUtil.setFieldValue(bean, "privateField", 2);
		assertThat(bean.inspectPrivateField()).isEqualTo(2);
		ReflectionUtil.setProperty(bean, "privateField", 3);
		assertThat(bean.inspectPrivateField()).isEqualTo(3);

		// 绕过将publicField+1的setter函数,直接设置publicField的原始值
		ReflectionUtil.setFieldValue(bean, "publicField", 2);
		assertThat(bean.inspectPublicField()).isEqualTo(2);

		// 没有绕过将publicField+1的setter函数
		ReflectionUtil.setProperty(bean, "publicField", 3);
		assertThat(bean.inspectPublicField()).isEqualTo(4);

		try {
			ReflectionUtil.getFieldValue(bean, "notExist");
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) { // NOSONAR
		}

		try {
			ReflectionUtil.setFieldValue(bean, "notExist", 2);
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) { // NOSONAR
		}
	}

	@Test
	public void invokeGetterAndSetter() {
		TestBean bean = new TestBean();
		assertThat((int) ReflectionUtil.invokeGetter(bean, "publicField")).isEqualTo(bean.inspectPublicField() + 1);

		bean = new TestBean();
		// 通过setter的函数将+1
		ReflectionUtil.invokeSetter(bean, "publicField", 10);
		assertThat(bean.inspectPublicField()).isEqualTo(10 + 1);
	}

	@Test
	public void invokeMethod() {
		TestBean bean = new TestBean();
		// 使用函数名+参数类型的匹配, 支持传参数
		assertThat((String) ReflectionUtil.invokeMethod(bean, "privateMethod", new Object[] { "calvin" }))
				.isEqualTo("hello calvin");

		// 使用函数名+参数类型的匹配
		assertThat((String) ReflectionUtil.invokeMethod(bean, "privateMethod", new Object[] { "calvin" },
				new Class[] { String.class })).isEqualTo("hello calvin");

		// 仅匹配函数名
		assertThat((String) ReflectionUtil.invokeMethodByName(bean, "privateMethod", new Object[] { "calvin" }))
				.isEqualTo("hello calvin");

		// 各种类型
		assertThat((int) ReflectionUtil.invokeMethod(bean, "intType", new Object[] { 1 }, new Class[] { int.class }))
				.isEqualTo(1);

		assertThat((int) ReflectionUtil.invokeMethod(bean, "integerType", new Object[] { 1 },
				new Class[] { Integer.class })).isEqualTo(1);

		assertThat((int) ReflectionUtil.invokeMethod(bean, "listType", new Object[] { ListUtil.newArrayList("1", "2") },
				new Class[] { List.class })).isEqualTo(2);

		assertThat((int) ReflectionUtil.invokeMethod(bean, "intType", 1)).isEqualTo(1);

		assertThat((int) ReflectionUtil.invokeMethod(bean, "integerType", 1)).isEqualTo(1);

		assertThat((int) ReflectionUtil.invokeMethod(bean, "listType", ListUtil.newArrayList("1", "2"))).isEqualTo(2);

		// 函数名错
		try {
			ReflectionUtil.invokeMethod(bean, "notExistMethod", new Object[] { "calvin" },
					new Class[] { String.class });
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) {

		}

		// 参数类型错
		try {
			ReflectionUtil.invokeMethod(bean, "privateMethod", new Object[] { "calvin" },
					new Class[] { Integer.class });
			failBecauseExceptionWasNotThrown(RuntimeException.class);
		} catch (RuntimeException e) {

		}

		// 函数名错
		try {
			ReflectionUtil.invokeMethodByName(bean, "notExistMethod", new Object[] { "calvin" });
			failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	public void invokeConstructor() {
		TestBean bean = ReflectionUtil.invokeConstructor(TestBean.class);
		assertThat(bean.getPublicField()).isEqualTo(2);

		TestBean3 bean3 = ReflectionUtil.invokeConstructor(TestBean3.class, 4);
		assertThat(bean3.getId()).isEqualTo(4);
	}

	@Test
	public void convertReflectionExceptionToUnchecked() {
		IllegalArgumentException iae = new IllegalArgumentException();
		// ReflectionException,normal
		RuntimeException e = ReflectionUtil.convertReflectionExceptionToUnchecked(iae);
		assertThat(e).isEqualTo(iae);

		// InvocationTargetException,extract it's target exception.
		Exception ex = new Exception();
		e = ReflectionUtil.convertReflectionExceptionToUnchecked(new InvocationTargetException(ex));
		assertThat(e.getCause()).isEqualTo(ex);

		// UncheckedException, ignore it.
		RuntimeException re = new RuntimeException("abc");
		e = ReflectionUtil.convertReflectionExceptionToUnchecked(re);
		assertThat(e).hasMessage("abc");

		// Unexcepted Checked exception.
		e = ReflectionUtil.convertReflectionExceptionToUnchecked(ex);
		assertThat(e).isInstanceOf(UncheckedException.class);
	}

	public static class ParentBean<T, ID> {
	}

	public static class TestBean extends ParentBean<String, Long> {
		/** 没有getter/setter的field */
		private int privateField = 1;
		/** 有getter/setter的field */
		private int publicField = 1;

		// 通過getter函數會比屬性值+1
		public int getPublicField() {
			return publicField + 1;
		}

		// 通過setter函數會被比輸入值加1
		public void setPublicField(int publicField) {
			this.publicField = publicField + 1;
		}

		public int inspectPrivateField() {
			return privateField;
		}

		public int inspectPublicField() {
			return publicField;
		}

		private String privateMethod(String text) {
			return "hello " + text;
		}

		// 测试原子类型转换
		public Integer integerType(Integer i) {
			return i;
		}

		// 测试原子类型转换
		public int intType(int i) {
			return i;
		}

		// 测试类型为接口
		public int listType(List<?> list) {
			return list.size();
		}
	}

	public static class TestBean2 extends ParentBean {
	}

	public static class TestBean3 {

		public TestBean3() {

		}

		public TestBean3(int id) {
			super();
			this.id = id;
		}

		private int id;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
	}
}
