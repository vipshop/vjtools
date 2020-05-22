package com.vip.vjtools.vjkit.datamasking.data;

import com.vip.vjtools.vjkit.datamasking.Sensitive;
import com.vip.vjtools.vjkit.datamasking.SensitiveType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ken
 */
public class TestChild {

	@Sensitive
	private String str;
	@Sensitive(type = SensitiveType.Hash)
	private String[] arr;
	@Sensitive(type = SensitiveType.Address)
	private List<String> list;
	@Sensitive(type = SensitiveType.Account)
	private Set<String> set;

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public String[] getArr() {
		return arr;
	}

	public void setArr(String[] arr) {
		this.arr = arr;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	public Set<String> getSet() {
		return set;
	}

	public void setSet(Set<String> set) {
		this.set = set;
	}

	@Override
	public String toString() {
		return "TestChild{" + "str='" + str + '\'' + ", arr=" + Arrays.toString(arr) + ", list=" + list + ", set=" + set
				+ '}';
	}
}
