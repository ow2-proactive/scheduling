package migration.test;

import org.objectweb.proactive.ProActive;

public class RemotePingPong {

  public static void main(String[] args) {

    SimpleAgent agent = null;

    if (args.length < 2) {
      System.err.println("Usage: TestFriends hostName/nodeName hostName2/nodeName2");
      System.exit(-1);
    }

    try {
      agent = (SimpleAgent)ProActive.newActive("migration.test.SimpleAgent", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ;

    System.out.println("Now starting test");

    for (int i = 0; i < 10; i++) {
      agent.moveTo(args[0]);
      //Thread.currentThread().sleep(100);
      agent.moveTo(args[1]);
      //Thread.currentThread().sleep(100);
      try {
        Thread.currentThread().sleep(1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
      ;
    }

    System.out.println("*************** C'est fini *************");
 

    // 	} catch (Exception e) {e.printStackTrace();}

  }
}
