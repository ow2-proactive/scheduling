package test.testStubInterface;

import java.lang.reflect.*;

import org.objectweb.proactive.core.mop.*;


public class ProxyOne implements org.objectweb.proactive.core.mop.Proxy {
    protected Object target;

    public ProxyOne(ConstructorCall constructorCall, Object[] parameters) {
        try {
            this.target = constructorCall.execute();
        } catch (Exception e) {
            e.printStackTrace();
            this.target = null;
        }
    }

    public Object reify(MethodCall c)
                 throws InvocationTargetException, IllegalAccessException {
        try {
            Object o = c.execute(target);
            return o;
        } catch (MethodCallExecutionFailedException e) {
            e.printStackTrace();
            return null;
        }
    }
}