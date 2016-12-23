package com.zhang.distributedlock;

import com.zhang.distributedlock.redis.RedisDistributedLock;

import redis.clients.jedis.Jedis;

public class CountRedisThread extends Thread {

	private int count = 800;

	Jedis jedis1 = new Jedis("localhost", 6379);

	Lock lock = new RedisDistributedLock(jedis1, "lock.lock", 500 * 1000);
	
	public CountRedisThread(String threadName) {
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
