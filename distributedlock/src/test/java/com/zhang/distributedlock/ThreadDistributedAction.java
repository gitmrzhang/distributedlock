package com.zhang.distributedlock;

public class ThreadDistributedAction extends Thread {

	private DistributedAction action;

	public ThreadDistributedAction(DistributedAction action) {
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
