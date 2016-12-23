package com.zhang.distributedlock.redis;

import java.util.concurrent.TimeUnit;

import com.zhang.distributedlock.AbstractLock;

import redis.clients.jedis.Jedis;

public class RedisDistributedLock extends AbstractLock {
	
	private Jedis jedis;
	// 锁的名称
	private String lockKey;
	// 锁有效时长ms
	private long lockExpires;

	public RedisDistributedLock(Jedis jedis, String lockKey, long lockExpires) {
		this.jedis = jedis;
		this.lockKey = lockKey;
		this.lockExpires = lockExpires;
	}
	
	@Override
	public void unlock() {
		unlock0();
	}

	@Override
	protected void unlock0() {
		String value = jedis.get(lockKey);
		if (!isTimeExpired(value)) {
			doUnlock();
		}
	}

	@Override
	protected boolean lock(boolean useTimeout, long time, TimeUnit unit, boolean interrupt)
			throws InterruptedException {
		if (interrupt) {
			checkInterruption();
		}
		// 超时时间
		long start = localTimeMillis();
		long timeout = localTimeMillis();
		if (useTimeout) {
			start = localTimeMillis();
			timeout = unit.toMillis(time);
		}
		while (useTimeout ? isTimeout(start, timeout) : true) {
			if (interrupt) {
				checkInterruption();
			}
			long lockExpireTime = localTimeMillis() + lockExpires + 1;// 锁超时时间
			String stringOfLockExpireTime = String.valueOf(lockExpireTime);
			if (jedis.setnx(lockKey, stringOfLockExpireTime) == 1) {// 获取到锁
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			String value = jedis.get(lockKey);
			if (isTimeExpired(value)) {// 锁超时
				String oldValue = jedis.getSet(lockKey, stringOfLockExpireTime);
				if (oldValue != null && isTimeExpired(oldValue)) {// 获取到锁  如果不是相同的线程  这样就会在释放锁的时候 抛出异常 
					setExclusiveOwnerThread(Thread.currentThread());
					return true;
				}
			}else{
				//没有超时 等待超时或者线程拥有者释放
			}
		}
		return false;
	}

	@Override
	public synchronized boolean tryLock() {
		long lockExpireTime = localTimeMillis() + lockExpires + 1;// 锁超时时间
		String stringOfLockExpireTime = String.valueOf(lockExpireTime);
		if (jedis.setnx(lockKey, stringOfLockExpireTime) == 1) {// 获取到锁
			setExclusiveOwnerThread(Thread.currentThread());
			return true;
		}
		String value = jedis.get(lockKey);
		if (isTimeExpired(value)) {// 锁超时
			String oldValue = jedis.getSet(lockKey, stringOfLockExpireTime);
			if (oldValue != null && isTimeExpired(oldValue)) {// 获取到锁  如果不是相同的线程  这样就会在释放锁的时候 抛出异常 
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断锁超时时间 超过则退出获取锁 等待下一次获取
	 * 
	 * @param start
	 * @param timeout
	 * @return
	 */
	private boolean isTimeout(long start, long timeout) {
		return start + timeout > localTimeMillis();
	}

	private long localTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * 判断锁是否在失效 true 失效 false 有效
	 * @param value
	 * @return
	 */
	private boolean isTimeExpired(String value) {
		if(value==null)
			return false;
		return Long.parseLong(value) < localTimeMillis();
	}

	private void doUnlock() {
		jedis.del(lockKey);
		setExclusiveOwnerThread(null);
	}

}
