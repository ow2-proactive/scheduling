package org.objectweb.proactive.core.util;

import java.io.Serializable;
import java.lang.reflect.Method;


public class SerializableMethod implements Serializable {
    private transient Method m;

    public SerializableMethod(Method m) {
        this.m = m;
    }

    public Method getMethod() {
        return m;
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
    	System.out.println("writing WrappedMethod");
        out.writeObject(m.getDeclaringClass());
        out.writeObject(m.getName());
        out.writeObject(m.getParameterTypes());
    }
    
    

//    private Class[] fixBugRead(FixWrapper[] para) {
//        Class[] tmp = new Class[para.length];
//        for (int i = 0; i < para.length; i++) {
//            //	System.out.println("fixBugRead for " + i + " value is " +para[i]);
//            tmp[i] = para[i].getWrapped();
//        }
//        return tmp;
//    }
//
//    private FixWrapper[] fixBugWrite(Class[] para) {
//        FixWrapper[] tmp = new FixWrapper[para.length];
//        for (int i = 0; i < para.length; i++) {
//            //	System.out.println("fixBugWrite for " + i + " out of " + para.length + " value is " +para[i] );	
//            tmp[i] = new MethodCall.FixWrapper(para[i]);
//        }
//        return tmp;
//    }
    
    private void readObject(java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
    	System.out.println("reading WrappedMethod");
        Class declaringClass = (Class) in.readObject();
        String name = (String) in.readObject();
        Class[] paramTypes = (Class[])in.readObject();

        try {
            m = declaringClass.getMethod(name, paramTypes);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

	@Override
	public int hashCode() {
		return m.hashCode();
	}

	@Override
	public String toString() {
		return m.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof SerializableMethod)) {
			return false;
		}
		return m.equals(((SerializableMethod)obj).getMethod());
	}
    
	
    
}
