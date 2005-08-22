package modelisation.test.customsocket;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class Caller {
    public static void main(String[] args) {
        ObjetDistantInterface od = null;
        while (true) {
            try {
                od = (ObjetDistantInterface) Naming.lookup(args[0]);
                od.echo();
                System.gc();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }

            //            try {
            //                Thread.currentThread().sleep(2000);
            //            } catch (InterruptedException e) {
            //            }
        }
    }
}
