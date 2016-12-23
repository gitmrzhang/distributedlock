package com.zhang.distributedlock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CountThread2 implements Runnable {

	private static int count = 800;

	private Lock lock = new ReentrantLock();


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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

}
