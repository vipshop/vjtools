package com.vip.vjtools.vjkit.io;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.google.common.io.Files;
import com.vip.vjtools.vjkit.base.Platforms;

public class FilePathUtilTest {

	char sep = Platforms.FILE_PATH_SEPARATOR_CHAR;

	@Test
	public void pathName() {
		String filePath = FilePathUtil.concat(sep + "abc", "ef");
		assertThat(filePath).isEqualTo(FilePathUtil.normalizePath("/abc/ef"));

		String filePath2 = FilePathUtil.concat(sep + "stuv" + sep, "xy");
		assertThat(filePath2).isEqualTo(FilePathUtil.normalizePath("/stuv/xy"));

		assertThat(FilePathUtil.simplifyPath("../dd/../abc")).isEqualTo("../abc");
		assertThat(FilePathUtil.simplifyPath("../../dd/../abc")).isEqualTo("../../abc");
		assertThat(FilePathUtil.simplifyPath("./abc")).isEqualTo("abc");

		assertThat(FilePathUtil.getParentPath(FilePathUtil.normalizePath("/abc/dd/efg/")))
				.isEqualTo(FilePathUtil.normalizePath("/abc/dd/"));

		assertThat(FilePathUtil.getParentPath(FilePathUtil.normalizePath("/abc/dd/efg.txt")))
				.isEqualTo(FilePathUtil.normalizePath("/abc/dd/"));
	}

	@Test
	public void getJarPath() {
		System.out.println("the jar file contains Files.class" + FilePathUtil.getJarPath(Files.class));
		assertThat(FilePathUtil.getJarPath(Files.class)).endsWith("guava-20.0.jar");
	}
}
