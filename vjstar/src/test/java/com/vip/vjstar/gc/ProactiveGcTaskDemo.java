package com.vip.vjstar.gc;

import java.io.IOException;

public class ProactiveGcTaskDemo {
	public static void main(String[] args) throws IOException {
		// 模拟内存占用
		final Enchanter enchanter = new Enchanter();
		enchanter.makeGarbage("50000000");
		
		CleanUpScheduler  scheduler = new CleanUpScheduler();
		ProactiveGcTask task = new ProactiveGcTask(scheduler, 50) ;
		scheduler.schedule("03:30-04:30", task);
	
		System.in.read();
	}
}
