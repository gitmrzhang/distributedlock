package com.zhang.distributedlock;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public abstract class AbstractLock implements Lock {

	private Logger logger = Logger.getLogger(AbstractLock.class);
	/**
	 * <pre>
	 *  
	 * 这里需不需要保证可见性值得讨论, 因为是分布式的锁,  
	 * 1.同一个jvm的多个线程使用不同的锁对象其实也是可以的, 这种情况下不需要保证可见性  
	 * 2.同一个jvm的多个线程使用同一个锁对象, 那可见性就必须要保证了.
	 * </pre>
	 */
	protected volatile boolean locked;

	/**
	 * 当前jvm内持有该锁的线程(if have one)
	 */
	private Thread exclusiveOwnerThread;

	protected final Thread getExclusiveOwnerThread() {
		return exclusiveOwnerThread;
	}

	protected void setExclusiveOwnerThread(Thread exclusiveOwnerThread) {
		if (logger.isDebugEnabled())
			if (exclusiveOwnerThread == null)
				logger.debug(Thread.currentThread().getName() + "释放锁");
			else
				logger.debug(Thread.currentThread().getName() + "获得锁,进行业务处理");

		this.exclusiveOwnerThread = exclusiveOwnerThread;
	}

	@Override
	public void lock() {
		try {
			lock(false, 0, null, false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		lock(false, 0, null, true);
	}

	@Override
	public boolean tryLock() {
		try {
			return lock(false, 0, null, false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		try {
			return lock(true, time, unit, false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean tryLockInterruptibly(long time, TimeUnit unit) throws InterruptedException {
		return lock(true, time, unit, true);
	}

	@Override
	public void unlock() {
		// 检查当前线程是否持有锁
		if (Thread.currentThread() != getExclusiveOwnerThread()) {
			throw new IllegalMonitorStateException("current thread does not hold the lock");
		}
		unlock0();
		setExclusiveOwnerThread(null);
	}

	/**
	 * 释放锁
	 */
	protected abstract void unlock0();

	/**
	 * 阻塞式获取锁
	 * 
	 * @param useTimeout
	 * @param time
	 * @param unit
	 * @param interrupt
	 *            是否响应中断
	 * @return
	 * @throws InterruptedException
	 */
	protected abstract boolean lock(boolean useTimeout, long time, TimeUnit unit, boolean interrupt)
			throws InterruptedException;

	protected void checkInterruption() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
	}
	
	/**
	 * 判断锁超时时间 超过则退出获取锁 等待下一次获取
	 * 
	 * @param start
	 * @param timeout
	 * @return
	 */
	protected boolean isTimeout(long start, long timeout) {
		return start + timeout > localTimeMillis();
	}
	/**
	 * 获取当前时间
	 * @return
	 */
	protected long localTimeMillis() {
		return System.currentTimeMillis();
	}

}
