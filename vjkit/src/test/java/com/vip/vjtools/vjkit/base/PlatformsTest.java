package com.vip.vjtools.vjkit.base;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class PlatformsTest {

	@Test
	public void PlatformTest() {

		if (Platforms.IS_WINDOWS) {
			assertThat(Platforms.FILE_PATH_SEPARATOR).isEqualTo("\\");
			assertThat(Platforms.FILE_PATH_SEPARATOR_CHAR).isEqualTo('\\');
		} else {
			assertThat(Platforms.FILE_PATH_SEPARATOR).isEqualTo("/");
			assertThat(Platforms.FILE_PATH_SEPARATOR_CHAR).isEqualTo('/');
		}

		System.out.println("OS_NAME:" + Platforms.OS_NAME);
		System.out.println("OS_VERSION:" + Platforms.OS_VERSION);
		System.out.println("OS_ARCH:" + Platforms.OS_ARCH);
		System.out.println("JAVA_SPECIFICATION_VERSION:" + Platforms.JAVA_SPECIFICATION_VERSION);
		System.out.println("JAVA_VERSION:" + Platforms.JAVA_VERSION);
		System.out.println("JAVA_HOME:" + Platforms.JAVA_HOME);
		System.out.println("USER_HOME:" + Platforms.USER_HOME);
		System.out.println("TMP_DIR:" + Platforms.TMP_DIR);
		System.out.println("WORKING_DIR:" + Platforms.WORKING_DIR);

		if (Platforms.IS_JAVA7) {
			assertThat(Platforms.IS_ATLEASET_JAVA7).isTrue();
			assertThat(Platforms.IS_ATLEASET_JAVA8).isFalse();
		}

		if (Platforms.IS_JAVA8) {
			assertThat(Platforms.IS_ATLEASET_JAVA7).isTrue();
			assertThat(Platforms.IS_ATLEASET_JAVA8).isTrue();
		}

	}


}
