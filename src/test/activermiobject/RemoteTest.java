package test.activermiobject;

import java.rmi.Remote;
import java.rmi.RemoteException;

interface RemoteTest extends Remote {

  public void echo() throws RemoteException;
}
