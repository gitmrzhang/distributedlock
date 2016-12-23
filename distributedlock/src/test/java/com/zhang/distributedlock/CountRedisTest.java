package com.zhang.distributedlock;


import org.junit.Test;

import com.zhang.distributedlock.util.ExecuteTimeUtil;

public class CountRedisTest {
	@Test
	public void test() throws InterruptedException{
		
		CountRedisThread thread1 = new CountRedisThread("redis1");
		CountRedisThread thread2 = new CountRedisThread("redis2");
		System.out.println("开始执行任务");
		ExecuteTimeUtil.start();
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		System.out.println("执行任务结束,用时"+ExecuteTimeUtil.executeTime());
	}

}
