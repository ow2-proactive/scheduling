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

	public ProcessForGroupCreation(ProxyForGroup proxyGroup, String className, Object[] param, String node) {
		this.proxyGroup = proxyGroup;
		this.className = className;
		this.param = param;
		this.node = node;
	}

	public void run() {
		try {
			proxyGroup.add(ProActive.newActive(className, param, node));
			proxyGroup.decrementWaitedAndNotifyAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
