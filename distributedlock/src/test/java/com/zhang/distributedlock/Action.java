package com.zhang.distributedlock;

import java.util.concurrent.locks.ReentrantLock;

public class Action {

	private static int count = 800;
	
	private ReentrantLock lock = new ReentrantLock();

	public void count() {
		try{
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
		}finally{
			lock.unlock();
		}
	}

	public int getCount() {
		return count;
	}


}
