package com.vip.vjtools.vjkit.datamasking.data;

import com.vip.vjtools.vjkit.datamasking.Sensitive;
import com.vip.vjtools.vjkit.datamasking.SensitiveType;

/**
 *
 * @author ken
 */
public class TestData {

	@Sensitive(type = SensitiveType.Name)
	private String name;

	@Sensitive(keepChars = 2)
	private String phone;

	@Sensitive(type = SensitiveType.Hash)
	private String hash;

	@Sensitive
	private String account;

	@Sensitive(keepChars = {1, 3})
	private String test;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}

	@Override
	public String toString() {
		return "TestData{" + "name='" + name + '\'' + ", phone='" + phone + '\'' + ", hash='" + hash + '\''
				+ ", account='" + account + '\'' + ", test='" + test + '\'' + '}';
	}
}
