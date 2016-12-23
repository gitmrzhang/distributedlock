package com.zhang.distributedlock;


import org.junit.Test;

import com.zhang.distributedlock.util.ExecuteTimeUtil;


public class CountReentrantTest {
	@Test
	public void test() throws InterruptedException{
		
		CountThread2 target = new CountThread2();
		Thread thread1 = new Thread(target);
		Thread thread2 = new Thread(target);
		System.out.println("开始执行任务");
		ExecuteTimeUtil.start();
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		System.out.println("执行任务结束,用时"+ExecuteTimeUtil.executeTime());
	}

}
