package migration.test;

import org.objectweb.proactive.ProActive;

public class TestHeavy {

  public static void main(String[] args) {
    //this test will proceed as follow
    //first we create a simple agent and make it 
    //migrate to the location specified on the commande line
    //then we send it 500 call to its echo() method
    SimpleAgent t = null;

    if (args.length < 1) {
      System.err.println("Usage: TestThread hostName/nodeName");
      System.exit(-1);
    }

    try {
      t = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    t.moveTo(args[0]);

    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }

    //now we ask the agent to sleep 
    t.sleep(5000);
    //and we send it requests
    System.out.println("TestHeavy: sending the requests");

    for (int i = 0; i < 500; i++) {
      System.out.println("Sending call " + i);
      t.echo();
    }
    //now we dump the methodTable 
    //ProActive.dumpCountTable();
  }
}
