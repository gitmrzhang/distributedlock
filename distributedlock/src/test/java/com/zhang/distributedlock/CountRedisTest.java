package com.zhang.distributedlock;

import org.junit.Test;

import com.zhang.distributedlock.redis.RedisDistributedLock;
import com.zhang.distributedlock.util.ExecuteTimeUtil;
import redis.clients.jedis.Jedis;

public class CountRedisTest {
	@Test
	public void test() throws InterruptedException {

		Jedis jedis1 = new Jedis("localhost", 6379);
		Lock lock = new RedisDistributedLock(jedis1, "lock.lock", 600 * 1000);//过期10分钟 
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
		System.out.println("执行任务结束,用时" + ExecuteTimeUtil.executeTime());
	}

}
