package com.vip.vjtools.vjkit.datamasking;

import com.alibaba.fastjson.JSON;
import com.vip.vjtools.vjkit.datamasking.data.TestData;
import com.vip.vjtools.vjkit.datamasking.data.TestUserMapingData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ken
 */
public class DataMaskJsonFilterTest {

	@Test
	public void testProcess() {
		DataMaskJsonFilter filter = new DataMaskJsonFilter();

		TestUserMapingData test1 = new TestUserMapingData();
		test1.setTel("13590908322");
		test1.setTest("test");

		//测试String[] 和 List
		test1.setStrArr(new String[]{"test1", "test2"});

		ArrayList<String> strList = new ArrayList<>();
		strList.add("test1");
		strList.add("test2");
		test1.setStrList(strList);

		//测试set
		Set<String> setStr = new HashSet<>();
		setStr.add("test1");
		test1.setSet(setStr);


		String json = JSON.toJSONString(test1, filter);
		//没有annotation的
		test1 = JSON.parseObject(json, TestUserMapingData.class);
		assertThat(test1.getTel()).isEqualTo("135*****322");
		assertThat(test1.getTest()).isEqualTo("test");

		assertThat(test1.getStrArr()).contains("t****");
		assertThat(test1.getStrList()).contains("t****");
		assertThat(test1.getSet()).contains("t****");

		//有annotation的
		TestData test2 = new TestData();
		test2.setName("name");
		test2.setPhone("13655555555");
		test2.setAccount("苹果");
		json = JSON.toJSONString(test2, filter);
		test2 = JSON.parseObject(json, TestData.class);

		assertThat(test2.getName()).isEqualTo("**me");
		assertThat(test2.getPhone()).isEqualTo("13*******55");
		assertThat(test2.getAccount()).isEqualTo("苹*");

	}
}
