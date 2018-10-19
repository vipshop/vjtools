package com.vip.vjtools.vjkit.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import com.google.common.io.Files;
import com.vip.vjtools.vjkit.base.Platforms;
import com.vip.vjtools.vjkit.number.RandomUtil;
import com.vip.vjtools.vjkit.text.Charsets;

public class FileUtilTest {

	@Test
	public void readWrite() throws IOException {
		File file = FileUtil.createTempFile("abc", ".tmp").toFile();
		try {
			String content = "haha\nhehe";
			FileUtil.write(content, file);

			String result = FileUtil.toString(file);
			assertThat(result).isEqualTo(content);
			List<String> lines = FileUtil.toLines(file);
			assertThat(lines).containsExactly("haha", "hehe");

			FileUtil.append("kaka", file);
			assertThat(new String(FileUtil.toByteArray(file), Charsets.UTF_8)).isEqualTo("haha\nhehekaka");
		} finally {
			FileUtil.deleteFile(file);
		}
	}

	@Test
	public void opFiles() throws IOException {
		File file = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testFile" + RandomUtil.nextInt()));
		FileUtil.touch(file);
		assertThat(FileUtil.isFileExists(file)).isTrue();
		FileUtil.touch(file);

		String content = "haha\nhehe";
		FileUtil.write(content, file);
		assertThat(FileUtil.toString(file)).isEqualTo(content);

		File newFile = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testFile" + RandomUtil.nextInt()));
		File newFile2 = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testFile" + RandomUtil.nextInt()));

		FileUtil.copyFile(file, newFile);
		assertThat(FileUtil.isFileExists(newFile)).isTrue();
		assertThat(FileUtil.toString(newFile)).isEqualTo(content);

		FileUtil.moveFile(newFile, newFile2);
		assertThat(FileUtil.toString(newFile2)).isEqualTo("haha\nhehe");


	}

	@Test
	public void opDir() throws IOException {
		String fileName = "testFile" + RandomUtil.nextInt();
		File dir = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testDir"));

		File file = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testDir", fileName));
		String content = "haha\nhehe";
		FileUtil.makesureDirExists(dir);
		FileUtil.write(content, file);

		File dir2 = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testDir2"));
		FileUtil.copyDir(dir, dir2);
		File file2 = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testDir2", fileName));
		assertThat(FileUtil.toString(file2)).isEqualTo("haha\nhehe");

		File dir3 = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testDir3"));
		FileUtil.moveDir(dir, dir3);
		File file3 = new File(FilePathUtil.concat(Platforms.TMP_DIR, "testDir3", fileName));
		assertThat(FileUtil.toString(file3)).isEqualTo("haha\nhehe");
		assertThat(FileUtil.isDirExists(dir)).isFalse();

	}

	@Test
	public void fileExist() throws IOException {
		assertThat(FileUtil.isDirExists(Platforms.TMP_DIR)).isTrue();
		assertThat(FileUtil.isDirExists(Platforms.TMP_DIR + RandomUtil.nextInt())).isFalse();

		File tmpFile = null;
		try {
			tmpFile = FileUtil.createTempFile().toFile();
			assertThat(FileUtil.isFileExists(tmpFile)).isTrue();

			assertThat(FileUtil.isFileExists(tmpFile.getAbsolutePath() + RandomUtil.nextInt())).isFalse();

		} finally {
			FileUtil.deleteFile(tmpFile);
		}
	}

	@Test
	public void getName() {

		assertThat(FileUtil.getFileName(FilePathUtil.normalizePath("/a/d/b/abc.txt"))).isEqualTo("abc.txt");
		assertThat(FileUtil.getFileName("abc.txt")).isEqualTo("abc.txt");

		assertThat(FileUtil.getFileExtension(FilePathUtil.normalizePath("a/d/b/abc.txt"))).isEqualTo("txt");
		assertThat(FileUtil.getFileExtension(FilePathUtil.normalizePath("/a/d/b/abc"))).isEqualTo("");
		assertThat(FileUtil.getFileExtension(FilePathUtil.normalizePath("/a/d/b/abc."))).isEqualTo("");

	}

	@Test
	public void testAsInputStream() throws Exception {
		Path tempPath = FileUtil.createTempFile();
		try (InputStream is = FileUtil.asInputStream(tempPath.toString());) {
			assertThat(is).isNotNull();
		}

		try (InputStream is = FileUtil.asInputStream(tempPath);) {
			assertThat(is).isNotNull();
		}

		try (InputStream is = FileUtil.asInputStream(tempPath.toFile());) {
			assertThat(is).isNotNull();
		}

	}

	@Test
	public void testAsOututStream() throws Exception {

		Path tempPath = FileUtil.createTempFile();
		try (OutputStream os = FileUtil.asOututStream(tempPath.toString())) {
			assertThat(os).isNotNull();
		}

		try (OutputStream os = FileUtil.asOututStream(tempPath);) {
			assertThat(os).isNotNull();
		}

		try (OutputStream os = FileUtil.asOututStream(tempPath.toFile())) {
			assertThat(os).isNotNull();
		}
	}

	@Test
	public void testAsBufferedReader() throws Exception {

		Path tempPath = FileUtil.createTempFile();
		try (BufferedReader br = FileUtil.asBufferedReader(tempPath.toString())) {
			assertThat(br).isNotNull();
		}

		try (BufferedReader br = FileUtil.asBufferedReader(tempPath)) {
			assertThat(br).isNotNull();
		}

	}

	@Test
	public void testAsBufferedWriter() throws Exception {

		Path tempPath = FileUtil.createTempFile();
		try (BufferedWriter bw = FileUtil.asBufferedWriter(tempPath.toString())) {
			assertThat(bw).isNotNull();
		}
		try (BufferedWriter bw = FileUtil.asBufferedWriter(tempPath)) {
			assertThat(bw).isNotNull();
		}
	}

	@Test
	public void testCopy() throws Exception {
		Path dir = FileUtil.createTempDir();

		assertThat(dir).exists();

		String srcFileName = "src";
		File srcFile = dir.resolve(srcFileName).toFile();
		FileUtil.touch(srcFile);

		assertThat(srcFile).exists();

		FileUtil.write("test", srcFile);

		String destFileName = "dest";

		File destFile = new File(dir.toFile(), "parent1/parent2/" + destFileName);
		FileUtil.makesureParentDirExists(destFile);

		FileUtil.copy(srcFile, destFile);

		assertThat(Files.readFirstLine(destFile, Charsets.UTF_8)).isEqualTo("test");
	}

	@Test
	public void testMakesureDirExists() throws Exception {
		Path dir = FileUtil.createTempDir();
		String child1 = "child1";

		Path child1Dir = dir.resolve(child1);
		FileUtil.makesureDirExists(child1Dir.toString());
		assertThat(child1Dir).exists();

		String child2 = "child2";
		Path child2Dir = dir.resolve(child2);
		FileUtil.makesureDirExists(child2Dir);
		assertThat(child2Dir).exists();

		String child3 = "child3";
		Path child3Dir = dir.resolve(child3);
		FileUtil.makesureDirExists(child3Dir.toFile());
		assertThat(child3Dir).exists();
	}

	@Test
	public void testIsFileExists() throws Exception {
		assertThat(FileUtil.isFileExists((String) null)).isFalse();
		assertThat(FileUtil.isFileExists((File) null)).isFalse();

		Path dir = FileUtil.createTempDir();
		FileUtil.touch(dir + "/" + "test");

		assertThat(FileUtil.isFileExists(dir + "/" + "test")).isTrue();

		assertThat(FileUtil.isFileExists(dir.resolve("test").toFile())).isTrue();

	}

	@Test
	public void testGetFileExtension() throws Exception {
		Path path = FileUtil.createTempFile("aaa", ".txt");

		assertThat(FileUtil.getFileExtension(path.toFile())).isEqualTo("txt");
		assertThat(FileUtil.getFileExtension(path.toString())).isEqualTo("txt");
	}

	@Test
	public void testIsDirExists() throws Exception {
		assertThat(FileUtil.isDirExists((String) null)).isFalse();
		assertThat(FileUtil.isDirExists((File) null)).isFalse();
		assertThat(FileUtil.isDirExists((Path) null)).isFalse();

		Path dir = FileUtil.createTempDir();

		assertThat(FileUtil.isDirExists(dir)).isTrue();
		assertThat(FileUtil.isDirExists(dir.toString())).isTrue();
		assertThat(FileUtil.isDirExists(dir.toFile())).isTrue();
	}

	@Test
	public void forceTouchTest() throws IOException {
		File file =FileUtil.forceTouch("/apps/test/logs/xxxx23/test.log");
		assertThat(FileUtil.isDirExists(new File(file.getParent()))).isTrue();
	}
}
