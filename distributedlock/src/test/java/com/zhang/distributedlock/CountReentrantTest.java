package com.zhang.distributedlock;


import org.junit.Test;

import com.zhang.distributedlock.util.ExecuteTimeUtil;


public class CountReentrantTest {
	@Test
	public void test() throws InterruptedException{
		
		Action action = new Action();
		
		ThreadAction thread1 = new ThreadAction(action) ;  
		ThreadAction thread2 = new ThreadAction(action) ;  
		ThreadAction thread3 = new ThreadAction(action) ;  
		
		System.out.println("开始执行任务");
		ExecuteTimeUtil.start();
		thread1.start();
		thread2.start();
		thread3.start();
		thread1.join();
		thread2.join();
		thread3.join();
		System.out.println("执行任务结束,用时"+ExecuteTimeUtil.executeTime());
	}

}
