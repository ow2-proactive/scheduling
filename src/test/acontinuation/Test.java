package test.acontinuation;

import java.util.Vector;
import org.objectweb.proactive.ProActive;

public class Test {

    public static void main (String args[]) {
        try{
        	
        	System.setProperty("proactive.future.ac","enable");
        	
            Dummy d1,d2;
            Dummy d = new Dummy();
            Agent a = (Agent)ProActive.newActive(Agent.class.getName(),new Object[]{});
            a.init();
            
            
            //System.out.println("La propriete proactive.future.ac=enable est deja mise par le programme !");
            d1=a.getDummyWait(d);
            d2=a.getDummy(d1);
            
            
            //d2 doit arriver avant d1
            Vector v = new Vector(2);
            v.add(d1);
            v.add(d2);
            
            if (ProActive.waitForAny(v)==0)
            	System.out.println("\n#### Automatic Continuation Mechanism is NOT working ####\n");
            else
            	System.out.println("\nAutomatic Continuation Mechanism is working\n");	
            //System.out.println(""+ProActive.waitForAny(v));
            
            ProActive.waitForAll(v);
            
         
            
        } catch (Exception e) {e.printStackTrace();}
        
    }

}

