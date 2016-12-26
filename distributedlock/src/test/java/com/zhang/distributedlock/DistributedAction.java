package com.zhang.distributedlock;



public class DistributedAction {

	private static int count = 800;

	private Lock lock;

	public DistributedAction(Lock lock) {
		this.lock = lock;
	}

	public void count() {
		try {
			lock.lock();
			for (int i = 0; i < 5; i++) {
				System.out.println(Thread.currentThread().getName() + " print count:" + getCount());
				count--;
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public int getCount() {
		return count;
	}

}
