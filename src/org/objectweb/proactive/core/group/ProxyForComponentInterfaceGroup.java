package org.objectweb.proactive.core.group;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.StubObject;


public class ProxyForComponentInterfaceGroup extends ProxyForGroup {
    protected InterfaceType interfaceType;
    protected Component owner;

    public ProxyForComponentInterfaceGroup()
        throws ConstructionOfReifiedObjectFailedException {
        super();
        className = Interface.class.getName();
    }

    public ProxyForComponentInterfaceGroup(ConstructorCall c, Object[] p)
        throws ConstructionOfReifiedObjectFailedException {
        super(c, p);
        className = Interface.class.getName();
    }

    public ProxyForComponentInterfaceGroup(String nameOfClass)
        throws ConstructionOfReifiedObjectFailedException {
        this();
        className = Interface.class.getName();
    }

    /*
     * @see org.objectweb.proactive.core.group.Group#getGroupByType()
     */
    public Object getGroupByType() {
        try {
            Interface result = ProActiveComponentGroup.newComponentInterfaceGroup(interfaceType,
                    owner);

            ProxyForComponentInterfaceGroup proxy = (ProxyForComponentInterfaceGroup) ((StubObject) result).getProxy();
            proxy.memberList = this.memberList;
            proxy.className = this.className;
            proxy.interfaceType = this.interfaceType;
            proxy.owner = this.owner;
            proxy.proxyForGroupID = this.proxyForGroupID;
            proxy.waited = this.waited;
            return result;
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
