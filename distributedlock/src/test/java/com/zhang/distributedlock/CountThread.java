package com.zhang.distributedlock;


public class CountThread extends Thread {

	private static int count = 800;

	private Lock lock;

	public CountThread(Lock lock, String threadName) {
		super(threadName);
		this.lock = lock;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < 10; i++) {
				lock.lock();
				System.out.println(Thread.currentThread().getName() + " print count:" + count);
				count--;
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}  finally {
			lock.unlock();
		}
	}

}
