package org.objectweb.proactive.core.group;

import java.util.Vector;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
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


	private static Proxy findLastProxy (Object obj) {
		if (!MOP.isReifiedObject(obj)) {
			return null;
		}
		Proxy proxy = ((StubObject) obj).getProxy();
		while (proxy instanceof FutureProxy) {
			if (MOP.isReifiedObject(((FutureProxy) proxy).getResult())) {
				return ProcessForOneWayCall.findLastProxy(((FutureProxy) proxy).getResult());
			}
			else {
				// consider the case where the result is not a reified object
				return proxy;
			}
		}
		return proxy;
	}


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
		boolean objectIsLocal = false;

		/* only do the communication (reify) if the object is not an error nor an exception */ 
		if (!(object instanceof Throwable)) {
			Proxy lastProxy = ProcessForOneWayCall.findLastProxy(object);
			if (lastProxy instanceof UniversalBodyProxy) {
				objectIsLocal = ((UniversalBodyProxy) lastProxy).isLocal();
			}
			try {
				if (lastProxy == null) {
					// means we are dealing with a non-reified object (in this implementation, 
					// it is the result of a future object, and the object is local.
					this.mc.execute(object);
				}
				else if (!objectIsLocal) {
					((StubObject) object).getProxy().reify(this.mc);
				}
				else {
					((StubObject) object).getProxy().reify(new MethodCall(this.mc));
				}
			} catch (Throwable e) {
				this.exceptionList.add(new ExceptionInGroup(object,e));
			}
		}
	}
}
