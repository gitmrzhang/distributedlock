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
	protected void unlock0() {
		String value = jedis.get(lockKey);
		if(!isTimeExpired(value)){
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
		if(useTimeout){
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
			if (value != null && isTimeExpired(value)) {// 锁在有效期内
				String oldValue = jedis.getSet(lockKey, stringOfLockExpireTime);
				if (oldValue != null && isTimeExpired(oldValue)) {// 获取到锁
					setExclusiveOwnerThread(Thread.currentThread());
					return true;
				}
			}else{
				//TODO 
			}
		}
		return false;
	}

	@Override
	public boolean tryLock() {
		long lockExpireTime = localTimeMillis() + lockExpires + 1;// 锁超时时间
		String stringOfLockExpireTime = String.valueOf(lockExpireTime);
		if (jedis.setnx(lockKey, stringOfLockExpireTime) == 1) {// 获取到锁
			setExclusiveOwnerThread(Thread.currentThread());
			return true;
		}
		String value = jedis.get(lockKey);
		if (value != null && isTimeExpired(value)) {// 锁在有效期内
			String oldValue = jedis.getSet(lockKey, stringOfLockExpireTime);
			if (oldValue != null && isTimeExpired(oldValue)) {// 获取到锁
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
		}
		return false;
	}

	private boolean isTimeout(long start, long timeout) {
		return start + timeout > localTimeMillis();
	}

	private long localTimeMillis() {
		return System.currentTimeMillis();
	}

	private boolean isTimeExpired(String value) {
		return Long.parseLong(value) < localTimeMillis();
	}
	
	private void doUnlock() {  
        jedis.del(lockKey);  
    }  

}
