package com.vip.vjtools.vjkit.datamasking;

import com.alibaba.fastjson.JSON;
import com.vip.vjtools.vjkit.datamasking.data.TestChild;
import com.vip.vjtools.vjkit.datamasking.data.TestData;
import com.vip.vjtools.vjkit.datamasking.data.TestParent;
import com.vip.vjtools.vjkit.datamasking.data.TestUserMapingData;
import com.vip.vjtools.vjkit.datamasking.strategy.HashMask;
import com.vip.vjtools.vjkit.text.EncodeUtil;
import com.vip.vjtools.vjkit.text.HashUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ken
 */
public class DataMaskTest {

	@Test
	public void testMaskByType() {
		assertThat(DataMask.mask("王守仁", SensitiveType.Name)).isEqualTo("**仁");
		assertThat(DataMask.mask("13599090990", SensitiveType.Phone)).isEqualTo("135*****990");
		assertThat(DataMask.mask("441421199902132221", SensitiveType.IDCard)).isEqualTo("44142***********21");
		assertThat(DataMask.mask("441421199902132221", SensitiveType.BankCard)).isEqualTo("4414************21");
		assertThat(DataMask.mask("广东省广州市荔湾区花地湾1号", SensitiveType.Address)).isEqualTo("广东省广州市荔湾区*****");
		assertThat(DataMask.mask("test@vipshop.com", SensitiveType.Email)).isEqualTo("t**t@vipshop.com");
		assertThat(DataMask.mask("9527", SensitiveType.Captcha)).isEqualTo("9**7");
		assertThat(DataMask.mask("441421199902132221", SensitiveType.Passport)).isEqualTo("44**************21");
		assertThat(DataMask.mask("9527", SensitiveType.Password)).isEqualTo("****");
		assertThat(DataMask.mask("account", SensitiveType.Account)).isEqualTo("a*****t");
		assertThat(DataMask.mask("default", SensitiveType.Default)).isEqualTo("d******");
		assertThat(DataMask.mask("test", SensitiveType.Hash)).isEqualTo(new HashMask().mask("test", null));
	}

	@Test
	public void testMask() {
		assertThat(DataMask.mask("test")).isEqualTo("t***");
	}

	@Test
	public void testSha1Mask() throws Exception {
		String hash = DataMask.mask("test", SensitiveType.Hash);
		System.out.println(hash);

		String salt = HashMask.getSalt();
		String encrypt =EncodeUtil.encodeHex(HashUtil.sha1("test"+salt));

		assertThat(hash).isNotNull().isEqualTo(encrypt);
	}

	@Test
	public void testToJson() {
		TestData testData = new TestData();
		testData.setName("123");
		testData.setPhone("1234567");
		testData.setAccount("test");
		testData.setHash("hash");
		testData.setTest("123456");

		String mask = DataMask.toJSONString(testData);
		System.out.println(mask);
		//直接用json转的
		testData.setName("**3");
		testData.setPhone("12***67");
		testData.setAccount("t***");
		testData.setTest("1**456");
		testData.setHash(new HashMask().mask("hash", null));
		assertThat(mask).isEqualTo(JSON.toJSONString(testData));
	}

	@Test
	public void testToString() {
		TestData testData = new TestData();
		testData.setName("123");
		testData.setPhone("1234567");
		testData.setAccount("test");
		testData.setHash("hash");
		testData.setTest("123456");
		String mask = DataMask.toString(testData);

		System.out.println(mask);

		testData.setName("**3");
		testData.setPhone("12***67");
		testData.setAccount("t***");
		testData.setTest("1**456");
		testData.setHash(new HashMask().mask("hash", null));
		assertThat(mask).isEqualTo(testData.toString());
	}

	@Test
	public void testMapping() {
		TestUserMapingData data = new TestUserMapingData();
		data.setNickName("nick");
		data.setTel("13590909090");

		String mask = DataMask.toString(data);
		System.out.println(mask);

		data.setNickName("**ck");
		data.setTel("135*****090");

		assertThat(mask).isEqualTo(data.toString());

	}

	//继承嵌套测试
	@Test
	public void testInherit() {
		TestChild child = new TestChild();
		child.setArr(new String[]{"test11111"});
		child.setStr("test11111");
		List<String> list = new ArrayList<>();
		list.add("test11111");
		child.setList(list);
		Set<String> set = new HashSet<>();
		set.add("test11111");
		child.setSet(set);

		TestParent parent = new TestParent();
		parent.setChild(child);
		parent.setOther(parent);

		child = new TestChild();
		child.setArr(new String[]{"test11111"});
		child.setStr("test11111");
		list = new ArrayList<>();
		list.add("test11111");
		child.setList(list);
		set = new HashSet<>();
		set.add("test11111");
		child.setSet(set);
		parent.setChildren(Arrays.asList(child));

		String json = DataMask.toJSONString(parent);
		System.out.println(json);

		//普通的子类
		parent = JSON.parseObject(json, TestParent.class);
		assertThat(parent.getChild().getStr()).contains("*");
		assertThat(parent.getChild().getArr()).contains("56C082E77E2924421F909BA262AA25BA80626323");
		assertThat(parent.getChild().getList()).contains("t********");
		assertThat(parent.getChild().getSet()).contains("t********");
		//子类list
		assertThat(parent.getChildren().get(0).getStr()).contains("*");
		assertThat(parent.getChildren().get(0).getArr()).contains("56C082E77E2924421F909BA262AA25BA80626323");
		assertThat(parent.getChildren().get(0).getList()).contains("t********");
		assertThat(parent.getChildren().get(0).getSet()).contains("t********");

		//验证下toString
		parent.setOther(null);//去掉循环
		assertThat(DataMask.toString(
				"TestParent{child=TestChild{str='t********', arr=[5489afe19ca3744d918d2821ed921e7bbc2b824b], list=[t********], set=[t********]}, children=[TestChild{str='t********', arr=[5489afe19ca3744d918d2821ed921e7bbc2b824b], list=[t********], set=[t********]}], other=null}"));
		System.out.println(DataMask.toString(parent));

	}
}
