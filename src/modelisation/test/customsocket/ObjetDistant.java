package modelisation.test.customsocket;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;

import org.objectweb.proactive.core.rmi.RandomPortSocketFactory;


public class ObjetDistant extends UnicastRemoteObject
    implements modelisation.test.customsocket.ObjetDistantInterface,
               Unreferenced {


protected static RandomPortSocketFactory factory = new RandomPortSocketFactory(37002, 5000);


    protected String bindName;
    protected long unbindTime;

    public ObjetDistant()
                 throws RemoteException {
       // super();
        super(0, factory, factory);
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
        }
         catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void echo()
              throws RemoteException {
        System.out.println("Echo.... at time " + System.currentTimeMillis());
    }

    public void unreferenced() {
        System.out.println(
                "Unreferenced  after " + 
                (System.currentTimeMillis() - this.unbindTime));
    }

    protected void finalize()
                     throws Throwable {
        System.out.println(
                " I am dying... after " + 
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