package test.multiplequeueserver;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.mop.StubObject;
import modelisation.multiqueueserver.MultiQueueServer;


public class Test implements Active {
    protected MultiQueueServer l;
 
    public Test() {}

    public Test(String serverUrl) {
	System.out.println("<init> " + serverUrl);
	try {
	    l = (MultiQueueServer) ProActive.lookupActive(MultiQueueServer.class.getName(), serverUrl);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	System.out.println("<init> server found " + l);
	//	((MultiQueueServer) l).echo();
    }

  //   public void updateLocation() {
// 	Body b = ProActive.getBodyOnThis();
// 	UniqueID i = b.getID();
// 	System.out.println("updateLocation() ");// + b + " " +i);
// 	l.updateLocation(i, b);  
// 	System.out.println("updateLocation() done");
//     }

    public void live(Body b) {
	System.out.println("live()");
	l.updateLocation(b.getID(), b.getRemoteAdapter());
	try {
	    Thread.sleep(500);
	} catch (Exception  e) {
	    e.printStackTrace();
	} // end of try-catch
	l.updateLocation(b.getID(), b.getRemoteAdapter());
    }

  //   public void test(Test t1, Test t2) {
// 	System.out.println("Registering first agent");
// 	t1.updateLocation();      
// 	System.out.println("Registering second agent");
// 	t2.updateLocation();    
//     }
    
    public static void main (String[] args) {
	if (args.length<1) {
	    System.err.println("Usage: java " + Test.class.getName() + " serverURL");  
	    System.exit(-1);
	} 


	//creation des objets actifs
	Test t1=null;
	Test t2=null;
	Object[] param = new Object[1];
	param[0] = args[0];
	
	try {
	    t1 = (Test) ProActive.newActive(Test.class.getName(),param);
	    t2 = (Test) ProActive.newActive(Test.class.getName(),param);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	
	//creation de l'objet test et debut du test
	// Test t = new Test();
// 	t.test(t1, t2);
	
    }
}
