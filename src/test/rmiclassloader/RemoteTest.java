package test.rmiclassloader;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RemoteTest extends Remote {
    public void test(Test t) throws RemoteException;
    public void receive(SerializableObjectInterface o) throws RemoteException;
}
