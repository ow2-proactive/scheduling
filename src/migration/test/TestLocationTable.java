package migration.test;

import org.objectweb.proactive.ProActive;

public class TestLocationTable {

  public static void main(String[] args) {
    if (!(args.length > 1)) {
      System.out.println("Usage: java migration.test.TestLocationTable hostname1/NodeName1 hostname2/NodeName2 ");
      System.exit(-1);
    }
    Friends t = null;
    Friends t2 = null;
    try {
      t = (Friends)ProActive.newActive("migration.test.Friends", null);
      t2 = (Friends)ProActive.newActive("migration.test.Friends", null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ;

    System.out.println("Now calling setFriend and getFutureFromFriend");

    t.setFriend(t2);
    t2.setFriend(t);
    t.getFutureFromFriend();

    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    //t.MoveTo("http://zephir.inria.fr/Node1");
    System.out.println("Now calling moveTo to send the agent on " + args[0] + " and then on " + args[1]);
    t2.moveTo(args[0]);
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }

    t2.moveTo(args[1]);

    try {
      Thread.currentThread().sleep(3000);
    } catch (Exception e) {
    }

    System.out.println("Now calling callFriend, this should lead to a forwarded call to echo() ");

    t.callFriend();

    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
    }

    System.out.println("Now calling moveTo, to join the other agent on " + args[1]);
    t.moveTo(args[1]);


    // 	try {
    // 	    Thread.currentThread().sleep(5000);
    // 	} catch (Exception e) {}

    // 	System.out.println("Now calling callFriend, this should not lead to a forwarded call to echo() ");
    // 	t.callFriend();


    try {
      Thread.currentThread().sleep(5000);
    } catch (Exception e) {
    }

    t2.callFriend();
    System.out.println("Test Over");
    //	System.exit(0); 
	
    //	System.out.println("The Active Object is now on host " + t.whereAreYou());
    //t.echo();
  }
}
