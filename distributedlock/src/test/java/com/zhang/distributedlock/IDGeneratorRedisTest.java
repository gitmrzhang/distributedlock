package com.zhang.distributedlock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.zhang.distributedlock.redis.RedisDistributedLock;

import redis.clients.jedis.Jedis;

public class IDGeneratorRedisTest {

	private static List<String> generatedIds = new ArrayList<String>();

	private static final String LOCK_KEY = "lock.lock";
	private static final long LOCK_EXPIRE = 5 * 1000;

	@Test
	public void testV1_0() throws Exception {

		Jedis jedis1 = new Jedis("localhost", 6379);
		Jedis jedis2 = new Jedis("localhost", 6379);
		Lock lock1 = new RedisDistributedLock(jedis1, LOCK_KEY, LOCK_EXPIRE);
		Lock lock2 = new RedisDistributedLock(jedis2, LOCK_KEY, LOCK_EXPIRE);
		IDGenerator g1 = new IDGenerator(lock1);
		IDConsumeTask consume1 = new IDConsumeTask(g1, "consume1");
		IDGenerator g2 = new IDGenerator(lock2);
		IDConsumeTask consume2 = new IDConsumeTask(g2, "consume2");

		Thread t1 = new Thread(consume1,"thread1");
		Thread t2 = new Thread(consume2,"thread2");
		t1.start();
		t2.start();

		Thread.sleep(20 * 1000); // 让两个线程跑20秒

		IDConsumeTask.stopAll();

		t1.join();
		t2.join();
	}

	static String time() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}

	static class IDConsumeTask implements Runnable {

		private IDGenerator idGenerator;

		private String name;

		private static volatile boolean stop;

		public IDConsumeTask(IDGenerator idGenerator, String name) {
			this.idGenerator = idGenerator;
			this.name = name;
		}

		public static void stopAll() {
			stop = true;
		}

		public void run() {
			System.out.println(time() + ": consume " + name + " start ");
			while (!stop) {
				String id = idGenerator.getAndIncrement();
				if (id != null) {
					if (generatedIds.contains(id)) {
						System.out.println(time() + ": duplicate id generated, id = " + id);
						stop = true;
						continue;
					}
					generatedIds.add(id);
					System.out.println(time() + ": consume " + name + " add id = " + id);
				}
			}
			System.out.println(time() + ": consume " + name + " done ");
		}

	}
}