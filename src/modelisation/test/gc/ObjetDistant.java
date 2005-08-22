package modelisation.test.gc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;


public class ObjetDistant extends UnicastRemoteObject
    implements modelisation.test.gc.ObjetDistantInterface, Unreferenced {
    protected String bindName;
    protected long unbindTime;

    public ObjetDistant() throws RemoteException {
        super();
    }

    public void createRegistry() {
        java.rmi.registry.LocateRegistry rg = null;
        try {
            LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
        }
    }

    public void register(String name) {
        this.bindName = name;
        try {
            Naming.rebind(name, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void echo() throws RemoteException {
        System.out.println("Echo.... at time " + System.currentTimeMillis());
        System.out.println("Unbinding ");
        try {
            Naming.unbind(this.bindName);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        this.unbindTime = System.currentTimeMillis();
        System.gc();
    }

    public void unreferenced() {
        System.out.println("Unreferenced  after " +
            (System.currentTimeMillis() - this.unbindTime));
    }

    protected void finalize() throws Throwable {
        System.out.println(" I am dying... after " +
            (System.currentTimeMillis() - this.unbindTime));
    }

    public static void main(String[] args) {
        ObjetDistant od = null;
        try {
            od = new ObjetDistant();
            od.createRegistry();
            od.register(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        od = null;
    }
}
