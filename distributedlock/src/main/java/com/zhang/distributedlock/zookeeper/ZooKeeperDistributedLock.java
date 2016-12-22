package com.zhang.distributedlock.zookeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import com.zhang.distributedlock.AbstractLock;

public class ZooKeeperDistributedLock extends AbstractLock {

	private ZkClient zkclient;

	private final String DEFAULT_ROOT_PATH = "/locks";
	
	private final String DEFAULT_SPLITSTR = "-";

	private String root = DEFAULT_ROOT_PATH;
	
	private long DEFAULT_TIME = 30000;
	
	private CountDownLatch latch = null;
	
	private long timeout = DEFAULT_TIME;
	
	private String lockName;

	private String myNode;//当前线程节点
	
	private String waitNode;//监听节点

	public ZooKeeperDistributedLock(ZkClient zkclient,String lockName) {
		this.zkclient = zkclient;
		this.lockName = lockName;
		createRoot();
	}

	public ZooKeeperDistributedLock(ZkClient zkclient,String lockName,String rootPath) {
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
			zkclient.createPersistent(root,new byte[0]);// 创建永久的根节点
		}
	}

	@Override
	protected void unlock0() {
		zkclient.delete(myNode);
		myNode=null; 
		//zkclient.close();
	}

	@Override
	protected boolean lock(boolean useTimeout, long time, TimeUnit unit, boolean interrupt)
			throws InterruptedException {
		if(interrupt){
			checkInterruption();
		}
		if(tryLock()){
			return true;
		}else{
			timeout = useTimeout?time:timeout;
			unit = unit==null?TimeUnit.MILLISECONDS:unit;
			waitForLock(waitNode,timeout,unit);
		}
		return tryLock();
	}
	
	private boolean waitForLock(String waitNode,long timeout,TimeUnit unit) throws InterruptedException{
		if(zkclient.exists(waitNode)){
			zkclient.subscribeChildChanges(waitNode, new IZkChildListener() {
				@Override
				public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
					System.out.println("监听子节点"+parentPath+"发生变化");
					tryLock();
				}
			});
			this.latch= new CountDownLatch(1);
			this.latch.await(timeout, unit);
			this.latch=null;
		}
		return tryLock();
	}

	@Override
	public boolean tryLock() {
		if(myNode == null){
			myNode = zkclient.createEphemeralSequential(root+"/"+lockName+DEFAULT_SPLITSTR, new byte[0]);
		}
		//遍历节点
		List<String> subNodes = zkclient.getChildren(root);
		List<String> lockObjNodes = new ArrayList<String>();
		for(String node : subNodes){
			String _node = node.split(DEFAULT_SPLITSTR)[0];
			if(lockName.equals(_node)){
				lockObjNodes.add(node);
			}
		}
		Collections.sort(lockObjNodes);//排序
		if(myNode.equals(root+"/"+lockObjNodes.get(0))){//该节点是最小节点 获得锁
			setExclusiveOwnerThread(Thread.currentThread());
			return true;
		}
		//如果不是最小的节点，找到比自己小1的节点
		String subMyZnode = myNode.substring(myNode.lastIndexOf("/") + 1);
		waitNode = root + "/" + lockObjNodes.get(Collections.binarySearch(lockObjNodes, subMyZnode) - 1);
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		if(this.tryLock())
			return true;
        try {
			 return waitForLock(waitNode,time,unit);
		} catch (InterruptedException e) {
			return false;
		}
	}
}
