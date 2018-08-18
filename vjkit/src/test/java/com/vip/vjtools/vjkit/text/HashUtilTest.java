package com.vip.vjtools.vjkit.text;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.vip.vjtools.vjkit.io.ResourceUtil;

public class HashUtilTest {


	@Test
	public void hashSha1() {
		// 普通
		String result = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah"));
		System.out.println("sha1:" + result);
		assertThat(result).isEqualTo("sCtJLx2IJNto032AhdkP64t/os4=");

		String result2 = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah".getBytes()));
		assertThat(result).isEqualTo("sCtJLx2IJNto032AhdkP64t/os4=");

		// 带盐, 每次salt值不一样，所以值也不一样。
		result = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah", HashUtil.generateSalt(5)));
		System.out.println("sha1 with salt:" + result);

		// 带盐，固定的盐
		result = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah", new byte[] { 1, 2, 3 }));
		assertThat(result).isEqualTo("U/7wy5R1sVrjEf3dOTAPz383g2k=");
		result2 = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah".getBytes(), new byte[] { 1, 2, 3 }));
		assertThat(result).isEqualTo(result2);

		// 带盐迭代, 每次salt值不一样，所以值也不一样。
		result = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah", HashUtil.generateSalt(5), 2));
		System.out.println("sha1 with salt with iteration:" + result);

		// 带盐迭代, 固定的盐
		result = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah", new byte[] { 1, 2, 3 }, 2));
		assertThat(result).isEqualTo("n9O7laits+ovoK8X8xde+XrsCtM=");
		result2 = EncodeUtil.encodeBase64(HashUtil.sha1("hhahah".getBytes(), new byte[] { 1, 2, 3 }, 2));
		assertThat(result).isEqualTo(result2);

	}

	@Test
	public void hashFile() throws IOException {
		InputStream in = ResourceUtil.asStream("test.txt");
		String result = EncodeUtil.encodeBase64(HashUtil.sha1File(in));
		assertThat(result).isEqualTo("DmSnwK/Fl0Jplrwtm9tfi7cb/js=");
		result = EncodeUtil.encodeBase64(HashUtil.md5File(in));
		assertThat(result).isEqualTo("1B2M2Y8AsgTpgAmY7PhCfg==");
	}

	@Test
	public void crc32() {
		assertThat(HashUtil.crc32AsInt("hahhha1")).isEqualTo(-625925593);
		assertThat(HashUtil.crc32AsInt("hahhha1".getBytes())).isEqualTo(-625925593);
		assertThat(HashUtil.crc32AsInt("hahhha2")).isEqualTo(1136161693);

		assertThat(HashUtil.crc32AsLong("hahhha1")).isEqualTo(3669041703L);
		assertThat(HashUtil.crc32AsLong("hahhha1".getBytes())).isEqualTo(3669041703L);
		assertThat(HashUtil.crc32AsLong("hahhha2")).isEqualTo(1136161693L);
	}

	@Test
	public void murmurhash() {
		assertThat(HashUtil.murmur32AsInt("hahhha1")).isEqualTo(-1920794701);
		assertThat(HashUtil.murmur32AsInt("hahhha1".getBytes())).isEqualTo(-1920794701);
		assertThat(HashUtil.murmur32AsInt("hahhha2")).isEqualTo(2065789419);
		assertThat(HashUtil.murmur32AsInt("hahhha3")).isEqualTo(-293065542);
		assertThat(HashUtil.murmur32AsInt("hahhha4")).isEqualTo(-2003559207);
		assertThat(HashUtil.murmur32AsInt("hahhha5")).isEqualTo(-3887993);
		assertThat(HashUtil.murmur32AsInt("hahhha6")).isEqualTo(-446760132);

		assertThat(HashUtil.murmur128AsLong("hahhha6")).isEqualTo(-5203515929515563680L);
		assertThat(HashUtil.murmur128AsLong("hahhha6".getBytes(Charsets.UTF_8))).isEqualTo(-5203515929515563680L);

	}

}
