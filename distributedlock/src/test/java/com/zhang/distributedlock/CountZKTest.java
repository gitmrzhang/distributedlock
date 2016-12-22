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
		ZkClient zkclient2 = new ZkClient(serverstring);
		Lock lock1 = new ZooKeeperDistributedLock(zkclient1, lockName);
		Lock lock2 = new ZooKeeperDistributedLock(zkclient2, lockName);
		
		CountThread thread1 = new CountThread(lock1,"zk1");
		CountThread thread2 = new CountThread(lock2,"zk2");
		System.out.println("开始执行任务");
		ExecuteTimeUtil.start();
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		System.out.println("执行任务结束,用时"+ExecuteTimeUtil.executeTime());
	}

}
