package modelisation.test.gc;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class Caller {
    public static void main(String[] args) {
        ObjetDistantInterface od = null;
        try {
            od = (ObjetDistantInterface) Naming.lookup(args[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        try {
            od.echo();
            od = null;
            System.out.println("Calling gc at time " +
                System.currentTimeMillis());
            //  System.gc();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}
