package com.zhang.distributedlock.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;

import com.zhang.distributedlock.AbstractLock;

public class ZooKeeperDistributedLock extends AbstractLock {
	
	private Logger logger = Logger.getLogger(ZooKeeperDistributedLock.class);

	private ZkClient zkclient;

	private final String DEFAULT_ROOT_PATH = "/locks";

	private final String DEFAULT_SPLITSTR = "-";

	private String root = DEFAULT_ROOT_PATH;

	private long DEFAULT_TIME = 30000;

	private CountDownLatch latch = null;

	private long timeout = DEFAULT_TIME;

	private String lockName;

	private String myNode;// 当前线程节点

	private String waitNode;// 监听节点

	private final Object mutex = new Object();

	public ZooKeeperDistributedLock(ZkClient zkclient, String lockName) {
		this.zkclient = zkclient;
		this.lockName = lockName;
		createRoot();
	}

	public ZooKeeperDistributedLock(ZkClient zkclient, String lockName, String rootPath) {
		this.zkclient = zkclient;
		this.lockName = rootPath;
		this.lockName = lockName;
		createRoot();
	}

	/**
	 * 创建根节点
	 */
	private void createRoot() {
		if (zkclient.exists(root)) {// 存在根节点
			return;
		} else {
			zkclient.createPersistent(root, new byte[0]);// 创建永久的根节点
		}
	}

	@Override
	protected void unlock0() {
		zkclient.delete(myNode);
		myNode = null;
		waitNode = null;
	}

	@Override
	protected boolean lock(boolean useTimeout, long time, TimeUnit unit, boolean interrupt)
			throws InterruptedException {
		if (interrupt) {
			checkInterruption();
		}
		long start = localTimeMillis();
		while (useTimeout ? isTimeout(start, timeout) : true) {
			if (tryLock()) {
				return true;
			} else {
				timeout = useTimeout ? time : timeout;
				unit = unit == null ? TimeUnit.MILLISECONDS : unit;
				if (useTimeout)
					return waitForLock(waitNode, timeout, unit);
				else
					waitForLock(waitNode);// 注册监听节点变化并且线程等待
			}
		}
		return false;
	}

	private void waitForLock(String waitNode) {
		if (zkclient.exists(waitNode)) {
			zkclient.subscribeChildChanges(waitNode, new IZkChildListener() {
				@Override
				public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
					if(logger.isDebugEnabled())
						logger.debug("监听子节点" + parentPath + "被删除,开始公平竞争锁");
					synchronized (mutex) {
						mutex.notify();
					}
				}
			});
			try {
				synchronized (mutex) {
					mutex.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 等待指定时间来获取锁
	 * 
	 * @param waitNode
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	private boolean waitForLock(String waitNode, long timeout, TimeUnit unit) throws InterruptedException {
		if (zkclient.exists(waitNode)) {
			zkclient.subscribeChildChanges(waitNode, new IZkChildListener() {
				@Override
				public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
					System.out.println("监听子节点" + parentPath + "被删除,开始公平竞争锁");
					tryLock();
				}
			});
			this.latch = new CountDownLatch(1);
			this.latch.await(timeout, unit);
			this.latch = null;
		}
		return tryLock();
	}

	@Override
	public boolean tryLock() {
		if (myNode == null) {
			myNode = zkclient.createEphemeralSequential(root + "/" + lockName + DEFAULT_SPLITSTR, new byte[0]);
			if(logger.isDebugEnabled())
				logger.debug(Thread.currentThread().getName()+"竞争锁，编号："+myNode);
		}
		// 遍历节点
		List<String> subNodes = zkclient.getChildren(root);
		List<String> lockObjNodes = new ArrayList<String>();
		for (String node : subNodes) {
			String _node = node.split(DEFAULT_SPLITSTR)[0];
			if (lockName.equals(_node)) {
				lockObjNodes.add(node);
			}
		}
		Collections.sort(lockObjNodes);// 排序
		if (myNode.equals(root + "/" + lockObjNodes.get(0))) {// 该节点是最小节点 获得锁
			setExclusiveOwnerThread(Thread.currentThread());
			return true;
		}
		// 如果不是最小的节点，找到比自己小1的节点
		String subMyZnode = myNode.substring(myNode.lastIndexOf("/") + 1);
		waitNode = root + "/" + lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		if (this.tryLock())
			return true;
		try {
			return waitForLock(waitNode, time, unit);
		} catch (InterruptedException e) {
			return false;
		}
	}

}
