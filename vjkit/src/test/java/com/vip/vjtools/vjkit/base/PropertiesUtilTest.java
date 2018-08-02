package com.vip.vjtools.vjkit.base;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.junit.Test;

public class PropertiesUtilTest {

	@Test
	public void loadProperties() {
		Properties p1 = PropertiesUtil.loadFromFile("classpath:application.properties");
		assertThat(p1.get("springside.min")).isEqualTo("1");
		assertThat(p1.get("springside.max")).isEqualTo("10");

		Properties p2 = PropertiesUtil.loadFromString("springside.min=1\nspringside.max=10\nisOpen=true");
		assertThat(PropertiesUtil.getInt(p2, "springside.min", 0)).isEqualTo(1);
		assertThat(PropertiesUtil.getInt(p2, "springside.max", 0)).isEqualTo(10);
		assertThat(PropertiesUtil.getInt(p2, "springside.maxA", 0)).isEqualTo(0);

		assertThat(PropertiesUtil.getLong(p2, "springside.min", 0L)).isEqualTo(1);
		assertThat(PropertiesUtil.getLong(p2, "springside.max", 0L)).isEqualTo(10);
		assertThat(PropertiesUtil.getLong(p2, "springside.maxA", 0L)).isEqualTo(0);

		assertThat(PropertiesUtil.getDouble(p2, "springside.min", 0d)).isEqualTo(1);
		assertThat(PropertiesUtil.getDouble(p2, "springside.max", 0d)).isEqualTo(10);
		assertThat(PropertiesUtil.getDouble(p2, "springside.maxA", 0d)).isEqualTo(0);

		assertThat(PropertiesUtil.getString(p2, "springside.min", "")).isEqualTo("1");
		assertThat(PropertiesUtil.getString(p2, "springside.max", "")).isEqualTo("10");
		assertThat(PropertiesUtil.getString(p2, "springside.maxA", "")).isEqualTo("");

		assertThat(PropertiesUtil.getBoolean(p2, "isOpen", false)).isTrue();
	}

}
