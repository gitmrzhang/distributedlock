package com.zhang.distributedlock;


import org.junit.Test;

import com.zhang.distributedlock.redis.RedisDistributedLock;
import com.zhang.distributedlock.util.ExecuteTimeUtil;

import redis.clients.jedis.Jedis;

public class CountRedisTest {
	private static final String LOCK_KEY = "lock.lock";
	private static final long LOCK_EXPIRE = 5 * 1000;
	@Test
	public void test() throws InterruptedException{
		
		Jedis jedis1 = new Jedis("localhost", 6379);
		Jedis jedis2 = new Jedis("localhost", 6379);
		Lock lock1 = new RedisDistributedLock(jedis1, LOCK_KEY, LOCK_EXPIRE);
		Lock lock2 = new RedisDistributedLock(jedis2, LOCK_KEY, LOCK_EXPIRE);
		
		CountThread thread1 = new CountThread(lock1,"redis1");
		CountThread thread2 = new CountThread(lock2,"redis2");
		System.out.println("开始执行任务");
		ExecuteTimeUtil.start();
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		System.out.println("执行任务结束,用时"+ExecuteTimeUtil.executeTime());
	}

}
