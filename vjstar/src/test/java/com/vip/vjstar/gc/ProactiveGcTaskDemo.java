package com.vip.vjstar.gc;

import java.io.IOException;

public class ProactiveGcTaskDemo {
	
	public static void main(String[] args) throws IOException {
		//// 真实代码示例 ////
		CleanUpScheduler scheduler = new CleanUpScheduler();
		ProactiveGcTask task = new ProactiveGcTask(scheduler, 50);
		scheduler.schedule("03:30-04:30", task);
		// ....
		scheduler.shutdown();

		///// 演示用代码 ////
		// 模拟内存占用, 根据运行环境调整到可以占用一半以上老生代
		final Enchanter enchanter = new Enchanter();
		enchanter.makeGarbage("10000000");

		// 直接运行看效果
		task.run();

		System.out.println("hit ENTER to stop");
		System.in.read();
		enchanter.clearGarbage();
		////////////////

	}
}
