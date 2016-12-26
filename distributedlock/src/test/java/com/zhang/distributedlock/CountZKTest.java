package com.zhang.distributedlock;

import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;

import com.zhang.distributedlock.util.ExecuteTimeUtil;
import com.zhang.distributedlock.zookeeper.ZooKeeperDistributedLock;


public class CountZKTest {
	@Test
	public void test() throws InterruptedException{
		
		String lockName = "data0";
		String serverstring = "127.0.0.1:2181";
		ZkClient zkclient1 = new ZkClient(serverstring);
		Lock lock = new ZooKeeperDistributedLock(zkclient1, lockName);
		DistributedAction action = new DistributedAction(lock);
		Thread thread1 = new ThreadDistributedAction(action);
		Thread thread2 = new ThreadDistributedAction(action);
		Thread thread3 = new ThreadDistributedAction(action);
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
