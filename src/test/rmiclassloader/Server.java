package test.rmiclassloader;

import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Server extends UnicastRemoteObject implements RemoteTest {
    public Server() throws RemoteException {

		System.setSecurityManager(new RMISecurityManager());
    }

    public void test(Test t) throws RemoteException {
    }

    public void receive(SerializableObjectInterface o) throws RemoteException {
        System.out.println("I have received " + o);
    }

    public void register(String name) {
        System.out.println(">>> Registering as " + name);

        try {
            Naming.rebind(name, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        System.out.println(">>> Registering done");
    }

    public static void main(String[] args) throws RemoteException {
        if (args.length < 1) {
            System.err.println("Usage: " + Server.class + " <bind name>");
            System.exit(0);
        }
        Server s = new Server();
        s.register(args[0]);
    }
}
