package com.vip.vjtools.vjkit.datamasking.data;

import com.vip.vjtools.vjkit.datamasking.Sensitive;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ken
 */
public class TestUserMapingData {

	private String nickName;//自定义的

	private String tel;//系统配置的

	private String test;//默认的

	@Sensitive
	private String[] strArr;

	@Sensitive
	private List<String> strList;

	@Sensitive
	private Set<String> set;

	@Sensitive
	private Set<Integer> setInt;

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}


	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}


	public String[] getStrArr() {
		return strArr;
	}

	public void setStrArr(String[] strArr) {
		this.strArr = strArr;
	}

	public List<String> getStrList() {
		return strList;
	}

	public void setStrList(List<String> strList) {
		this.strList = strList;
	}


	public Set<String> getSet() {
		return set;
	}

	public void setSet(Set<String> set) {
		this.set = set;
	}

	public Set<Integer> getSetInt() {
		return setInt;
	}

	public void setSetInt(Set<Integer> setInt) {
		this.setInt = setInt;
	}

	@Override
	public String toString() {
		return "TestUserMapingData{" + "nickName='" + nickName + '\'' + ", tel='" + tel + '\'' + ", test='" + test
				+ '\'' + ", strArr=" + Arrays.toString(strArr) + ", strList=" + strList + ", set=" + set + ", setInt="
				+ setInt + '}';
	}
}
