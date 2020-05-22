package com.vip.vjtools.vjkit.datamasking;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ken
 */
public class MaskMappingTest {

	@Test
	public void test() {
		//系统定义的
		assertThat(MaskMapping.getMaskTypeMapping("tel")).isNotNull();
		//自定义添加的
		assertThat(MaskMapping.getMaskTypeMapping("nickName")).isNotNull();
	}

}
