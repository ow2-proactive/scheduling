package modelisation.test.gc;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ObjetDistantInterface extends Remote {
    public void echo() throws RemoteException;
}
