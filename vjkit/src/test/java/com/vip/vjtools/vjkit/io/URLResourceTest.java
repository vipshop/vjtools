package com.vip.vjtools.vjkit.io;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class URLResourceTest {

	@Test
	public void resource() throws IOException {
		File file = URLResourceUtil.asFile("classpath:application.properties");
		assertThat(FileUtil.toString(file)).isEqualTo("springside.min=1\nspringside.max=10");

		InputStream is = URLResourceUtil.asStream("classpath:application.properties");
		assertThat(IOUtil.toString(is)).isEqualTo("springside.min=1\nspringside.max=10");
		IOUtil.closeQuietly(is);

		try {
			URLResourceUtil.asFile("classpath:notexist.properties");
			fail("should fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			URLResourceUtil.asStream("classpath:notexist.properties");
			fail("should fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

	}

	@Test
	public void file() throws IOException {
		File file = FileUtil.createTempFile().toFile();
		FileUtil.write("haha", file);

		try {
			File file2 = URLResourceUtil.asFile("file://" + file.getAbsolutePath());
			assertThat(FileUtil.toString(file2)).isEqualTo("haha");

			File file2NotExist = URLResourceUtil.asFile("file://" + file.getAbsolutePath() + ".noexist");
			assertThat(file2NotExist.exists()).isFalse();

			File file3 = URLResourceUtil.asFile(file.getAbsolutePath());
			assertThat(FileUtil.toString(file3)).isEqualTo("haha");
			File file3NotExist = URLResourceUtil.asFile(file.getAbsolutePath() + ".noexist");
			assertThat(file3NotExist.exists()).isFalse();
		} finally {
			FileUtil.deleteFile(file);
		}

	}

}
