package test.rmiclassloader;

import java.rmi.RemoteException;


public class SerializableObject implements java.io.Serializable, SerializableObjectInterface {

    // protected InnerClass inner;
    protected SerializableObject2 o2 = null; 

    public SerializableObject() throws RemoteException {
    }

    public SerializableObject(String s) throws RemoteException {
        //       this.inner = new InnerClass(s);
        try {
        
		o2 = new SerializableObject2();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }

    public String toString() {
        //      return this.inner.toString();
        return super.toString();
    }

    private class InnerClass implements java.io.Serializable {

        protected String name;

        protected InnerClass() {
        }

        protected InnerClass(String s) {
            this.name = s;
        }

        public String toString() {
            return this.name;
        }
    }
}
