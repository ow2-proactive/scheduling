package org.objectweb.proactive.core.group;

import java.util.Vector;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;

/**
 * This class provides multithreading for the oneway methodcall on a group.
 * 
 * @author Laurent Baduel
  */
public class ProcessForOneWayCall implements Runnable {
	private ProxyForGroup proxyGroup;
	private Vector memberList;
	private int index;
	private MethodCall mc;
	private Body body;

	public ProcessForOneWayCall(ProxyForGroup proxyGroup, Vector memberList, int index, MethodCall mc, Body body) {
		this.proxyGroup = proxyGroup;
		this.memberList = memberList;
		this.index = index;
		this.mc = mc;
		this.body = body;
	}

	public synchronized void run() {
		try {
				LocalBodyStore.getInstance().setCurrentThreadBody(body);
				((StubObject) (memberList.get(index))).getProxy().reify(mc);
				proxyGroup.decrementWaitedAndNotifyAll();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
