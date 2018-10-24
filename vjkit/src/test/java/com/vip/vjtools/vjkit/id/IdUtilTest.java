package com.vip.vjtools.vjkit.id;

import java.util.UUID;

import com.vip.vjtools.vjkit.net.NetUtil;
import org.junit.Test;

public class IdUtilTest {

	@Test
	public void normal() {
		UUID id1 = IdUtil.fastUUID();
		UUID id2 = IdUtil.fastUUID();
		System.out.println("UUID1:" + id1);
		System.out.println("UUID2:" + id2.toString());
	}

	@Test
	public void generateIdTest() {
		long start = System.currentTimeMillis();
		for(int i=0;i<9;i++){
//			UUID id = IdUtil.fastUUID();
//			id.toString();
			IdUtil.generateId();
		}
		long end = System.currentTimeMillis();
		System.out.println("耗时："+(end - start)+" 毫秒");
	}
}
