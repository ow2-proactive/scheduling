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
	private ExceptionList exceptionList;

	public ProcessForOneWayCall(ProxyForGroup proxyGroup, Vector memberList, int index, MethodCall mc, Body body, ExceptionList exceptionList) {
		this.proxyGroup = proxyGroup;
		this.memberList = memberList;
		this.index = index;
		this.mc = mc;
		this.body = body;
		this.exceptionList = exceptionList;
	}

	public synchronized void run() {
		LocalBodyStore.getInstance().setCurrentThreadBody(body);
		Object object = this.memberList.get(this.index);
		try {
			((StubObject) object).getProxy().reify(this.mc);
		} catch (Throwable e) {
			this.exceptionList.add(new ExceptionInGroup(object,e));
		}
		finally {
			this.proxyGroup.decrementWaitedAndNotifyAll();			
		}
	}
}
