package migration.bench2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import org.objectweb.proactive.ProActive;


public class Bench {
    public static Agent[] createAgents(int numberAgents, Vector v) {
        Agent[] allAgents = new Agent[numberAgents];

        Object[] parameters = new Object[1];
        int currentAgent;
        int j;

        //first we create the agents
        // and set their destination
        for (currentAgent = 0; currentAgent < numberAgents; currentAgent++) {
            try {
                parameters[0] = new Integer(currentAgent);
                allAgents[currentAgent] = (Agent) ProActive.newActive(Agent.class.getName(),
                        parameters);
                allAgents[currentAgent].setDestinationList(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //then we sent their references
        for (currentAgent = 0; currentAgent < numberAgents; currentAgent++) {
            for (j = 0; j < numberAgents; j++) {
                if (j != currentAgent) {
                    allAgents[currentAgent].addAgent(allAgents[j]);
                }
            }
        }

        return allAgents;
    }

    public static void startAll(Agent[] allAgents) {
        int i;
        for (i = 0; i < allAgents.length; i++) {
            allAgents[i].start();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java " + Bench.class.getName() +
                " number fileName");
            System.exit(-1);
        }

        //now we get the destination list from a file
        Vector list = new Vector();
        try {
            FileInputStream file = new FileInputStream(args[1]);
            BufferedReader in = new BufferedReader(new InputStreamReader(file));

            //tant qu'on peut lire
            while (in.ready()) {
                list.addElement(in.readLine());
            }
            file.close();
        } catch (IOException ex) {
            System.err.println("Can't read from `" + ex.getMessage() + "'");
            System.exit(1);
        }

        Agent[] tablo = Bench.createAgents(Integer.parseInt(args[0]), list);

        Bench.startAll(tablo);

        try {
            Thread.sleep(50000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    // 	Agent agent1 = null;
    // 	Agent agent2 = null;
    // 	Agent agent3 = null;
    // 	Agent agent4 = null;
    // 	try {	
    // 	    Object tablo[] = new Object[1];
    // 	    tablo[0] = new Integer(1);
    // 	    //we create the agents
    // 	    agent1 = (Agent) ProActive.newActive("migration.bench2.Agent", tablo); 
    // 	    tablo[0] = new Integer(2);
    // 	    agent2 = (Agent) ProActive.newActive("migration.bench2.Agent", tablo );
    // 	    tablo[0] = new Integer(3);
    // 	    agent3 = (Agent) ProActive.newActive("migration.bench2.Agent", tablo );
    // 	    tablo[0] = new Integer(4);
    // 	    agent4 = (Agent) ProActive.newActive("migration.bench2.Agent", tablo );
    // 	} catch (Exception e) {
    // 	    System.out.println("Exception : " + e);
    // 	    e.printStackTrace(); 
    // 	    System.exit(-1);
    // 	}
    // 	//we sould find a better solution for this...
    // 	agent1.addAgent(agent2);
    // 	agent1.addAgent(agent3);
    // 	agent1.addAgent(agent4);
    // 	agent2.addAgent(agent1);
    // 	agent2.addAgent(agent3);
    // 	agent2.addAgent(agent4);
    // 	agent3.addAgent(agent1);
    // 	agent3.addAgent(agent2);
    // 	agent3.addAgent(agent4);
    // 	agent4.addAgent(agent1);
    // 	agent4.addAgent(agent2);
    // 	agent4.addAgent(agent3);
    // 	//now we get the destination list from a file
    // 	Vector list = new Vector();
    // 	try
    // 	    {
    // 		FileInputStream file = new FileInputStream(args[0]);
    // 		BufferedReader in =
    // 		    new BufferedReader(new InputStreamReader(file));  
    // 		//tant qu'on peut lire
    // 		while (in.ready()) {
    // 		    list.addElement(in.readLine());
    // 		}
    // 		file.close();
    // 	    }
    // 	catch(IOException ex)
    // 	    {
    // 		System.err.println("Can't read from `" +
    // 				   ex.getMessage() + "'");
    // 		System.exit(1);
    // 	    }
    // 	//hopefully it worked
    // 	System.out.println("Setting destination list");
    // 	agent1.setDestinationList(list);
    //  	agent2.setDestinationList(list);
    // 	agent3.setDestinationList(list);
    // 	agent4.setDestinationList(list);
    // 	//and we go
    // 	agent1.start();
    // 	agent2.start();
    // 	agent3.start();
    // 	agent4.start();
    // 	try {
    // 	    Thread.currentThread().sleep(5000);
    // 	} catch (Exception e) {e.printStackTrace();}
    //     }
}
