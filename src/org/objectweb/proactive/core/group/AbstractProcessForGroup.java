package org.objectweb.proactive.core.group;

import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;

import java.util.Vector;


public abstract class AbstractProcessForGroup {
    protected static Proxy findLastProxy(Object obj) {
        if (!MOP.isReifiedObject(obj)) {
            return null;
        }
        Proxy proxy = ((StubObject) obj).getProxy();
        while (proxy instanceof FutureProxy) {
            if (MOP.isReifiedObject(((FutureProxy) proxy).getResult())) {
                return AbstractProcessForGroup.findLastProxy(((FutureProxy) proxy).getResult());
            } else {
                return proxy;
            }
        }
        return proxy;
    }

    protected Vector memberList;
    protected ProxyForGroup proxyGroup;

    public int getMemberListSize() {
        if (memberList != null) {
            return memberList.size();
        } else {
            return 0;
        }
    }
}
