package test.rmiclassloader;

import java.net.MalformedURLException;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;


public class Test implements java.io.Serializable {
    //  protected InnerClass myInnerClass;
    public Test() {
        //    this.myInnerClass = new InnerClass();
        System.setSecurityManager(new RMISecurityManager());
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage:" + Test.class.getName() +
                " <serverName>");
            System.exit(0);
        }

        RemoteTest server = null;

        //  System.out.println("A simple test which serialize an inner class");
        try {
            server = (RemoteTest) Naming.lookup(args[0]);
            server.receive(new SerializableObject("test"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        //        Test test = new Test();
        //     test.startSerialization();
    }
}
