package org.objectweb.proactive.core.group;

import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides multithreading for the (a)synchronous methodcall on a group.
 * 
 * @author Laurent Baduel
 */

public class ProcessForAsyncCall implements Runnable {
	private ProxyForGroup proxyGroup;
	private Vector memberList;
	private Vector memberListOfResultGroup;
	private int index;
	private MethodCall mc;
	private Body body;

	public ProcessForAsyncCall(ProxyForGroup proxyGroup, Vector memberList, Vector memberListOfResultGroup, int index, MethodCall mc, Body body) {
		this.proxyGroup = proxyGroup;
		this.memberList = memberList;
		this.memberListOfResultGroup = memberListOfResultGroup;
		this.index = index;
		this.mc = mc;
		this.body = body;
	}

	public synchronized void run() {
		try {
				LocalBodyStore.getInstance().setCurrentThreadBody(body);
				proxyGroup.addToListOfResult(memberListOfResultGroup, ((StubObject) (memberList.get(index))).getProxy().reify(mc), index);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
