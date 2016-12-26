package com.zhang.distributedlock;

public class ThreadAction extends Thread {

	private Action action;

	public ThreadAction(Action action) {
		this.action = action;
	}

	@Override
	public void run() {
		for(int i = 0 ;i<5;i++){
			action.count();
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
