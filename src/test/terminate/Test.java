package test.terminate;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

public class Test implements org.objectweb.proactive.RunActive {

    public Test() {}


    public void runActivity(Body body) {
	//	b.fifoPolicy();
	body.serve(body.getRequestQueue().blockingRemoveOldest());
	body.terminate();
    }

    public void echo() {
	System.out.println("Echo()");
    }

    public static void main (String[] args) {
	Test t = null;
	try {
	   t= (Test) ProActive.newActive(Test.class.getName(), (Object[]) null);
	} catch (Exception e) {
	    e.printStackTrace();
	} // end of try-catch
	while (true) {
	    t.echo();	 
	} // end of while (true)
	
    }
    


}
