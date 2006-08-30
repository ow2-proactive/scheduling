package org.objectweb.proactive.core.component.group;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.group.ProActiveComponentGroup;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.StubObject;

/**
 * An extension of the standard group proxy for handling groups of components.
 * 
 * @author Matthieu Morel
 *
 */
public class ProxyForComponentGroup extends ProxyForGroup {
    protected ComponentType componentType;
    protected ControllerDescription controllerDesc;

    public ProxyForComponentGroup()
        throws ConstructionOfReifiedObjectFailedException {
        super();
        className = Component.class.getName();
    }

    public ProxyForComponentGroup(ConstructorCall c, Object[] p)
        throws ConstructionOfReifiedObjectFailedException {
        super(c, p);
        className = Component.class.getName();
    }

    public ProxyForComponentGroup(String nameOfClass)
        throws ConstructionOfReifiedObjectFailedException {
        super(nameOfClass);
        className = Component.class.getName();
    }

    /*
     * @see org.objectweb.proactive.core.group.Group#getGroupByType()
     */
    public Object getGroupByType() {
        try {
            Component result = ProActiveComponentGroup.newComponentRepresentativeGroup(componentType,
                    controllerDesc);

            ProxyForComponentGroup proxy = (ProxyForComponentGroup) ((StubObject) result).getProxy();
            proxy.memberList = this.memberList;
            proxy.className = this.className;
            proxy.componentType = this.componentType;
            proxy.controllerDesc = this.controllerDesc;
            proxy.proxyForGroupID = this.proxyForGroupID;
            proxy.waited = this.waited;
            return result;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    
    /**
     * @return Returns the componentType.
     */
    public ComponentType getComponentType() {
    
        return componentType;
    }

    
    /**
     * @param componentType The componentType to set.
     */
    public void setComponentType(ComponentType componentType) {
    
        this.componentType = componentType;
    }

    
    /**
     * @return Returns the controllerDesc.
     */
    public ControllerDescription getControllerDesc() {
    
        return controllerDesc;
    }

    
    /**
     * @param controllerDesc The controllerDesc to set.
     */
    public void setControllerDesc(ControllerDescription controllerDesc) {
    
        this.controllerDesc = controllerDesc;
    }
}
