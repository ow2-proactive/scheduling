package test.rmiclassloader;

import java.rmi.RemoteException;


public class SerializableObject2 implements java.io.Serializable {
   // protected InnerClass inner;

    public SerializableObject2() throws RemoteException {
    }

    public SerializableObject2(String s) throws RemoteException {
 //       this.inner = new InnerClass(s);
    }

    public String toString() {
  //      return this.inner.toString();
  return super.toString();
    }

}
