package com.vip.vjtools.vjkit.id;

import java.util.Random;
import java.util.UUID;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

	@Test
	public void increaseIdTest(){
		IdUtil.setStartValue(10000L);
		for(int i=0;i<9999999;i++){
			long id = IdUtil.increaseId(new String("test1"));
//			System.out.println(id);
			long id2 = IdUtil.increaseId("test2");
//			System.out.println(id2);
		}
	}
}
