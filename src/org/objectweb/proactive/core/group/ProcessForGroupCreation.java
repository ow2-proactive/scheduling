package org.objectweb.proactive.core.group;

import org.objectweb.proactive.ProActive;

/**
 * This class provides multithreading for the creation of active objects.
 * 
 * @author Laurent Baduel
 */
public class ProcessForGroupCreation implements Runnable {
	private ProxyForGroup proxyGroup;
	private String className;
	private Object[] param;
	private String node;
	private int index;

	public ProcessForGroupCreation(ProxyForGroup proxyGroup, String className, Object[] param, String node, int index) {
		this.proxyGroup = proxyGroup;
		this.className = className;
		this.param = param;
		this.node = node;
		this.index = index;
	}

	public void run() {
		try {
			this.proxyGroup.add(this.index,ProActive.newActive(className, param, node));
			this.proxyGroup.decrementWaitedAndNotifyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
