package test.acontinuation;

import org.objectweb.proactive.ProActive;

public class Test {

    public static void main (String args[]) {
        try{
        	
        	System.setProperty("proactive.future.ac","enable");
        	
            Dummy d;
            Agent a = (Agent)ProActive.newActive(Agent.class.getName(),new Object[]{});
            a.init();
            
            System.out.println("La propriete proactive.future.ac=enable est deja mise par le programme !");
            d=a.getDummy();
            a.print();
            //System.out.println("Après...");
        } catch (Exception e) {e.printStackTrace();}
        
    }

}

