package com.zhang.distributedlock;

import org.I0Itec.zkclient.ZkClient;

import com.zhang.distributedlock.zookeeper.ZooKeeperDistributedLock;

public class CountZKThread extends Thread {

	private int count = 800;
	String lockName = "data0";
	String serverstring = "127.0.0.1:2181";
	ZkClient zkclient1 = new ZkClient(serverstring);
	Lock lock = new ZooKeeperDistributedLock(zkclient1, lockName);

	public CountZKThread(String threadName) {
		super(threadName);
	}

	@Override
	public void run() {
		try {
			lock.lock();
			for (int i = 0; i < 10; i++) {
				System.out.println(Thread.currentThread().getName() + " print count:" + count);
				count--;
				Thread.sleep(300);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

}
