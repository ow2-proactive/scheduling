package migration.test;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

//This is to test the migration to the location of 
//another active object
//we try to join an object on the same JVM, then another one 
//on a different one

public class TestFriends {

  public static void main(String[] args) {
    Friends f1 = null;
    Friends f2 = null;
    Friends f3 = null;

    if (args.length < 1) {
      System.err.println("Usage: TestFriends hostName/nodeName");
      System.exit(-1);
    }

    try {

      f1 = (Friends)ProActive.newActive("migration.test.Friends", null);
      f2 = (Friends)ProActive.newActive("migration.test.Friends", null);
      f3 = (Friends)ProActive.newActive("migration.test.Friends", null, NodeFactory.getNode(args[0]));
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("Creation of all active objects finished");
    System.out.println("Now asking to join friend on the same JVM");
    f2.meetFriend(f1);

    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    f2.echo();

    System.out.println("Now we give it another try  on the same JVM");
    f2.meetFriend(f1);
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    System.out.println("Now asking to join friend on another JVM");
    f2.meetFriend(f3);
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    f2.echo();

    System.out.println("Now we move to this friend again ");
    f2.meetFriend(f3);
    try {
      Thread.currentThread().sleep(2000);
    } catch (Exception e) {
    }
    f2.echo();
  }
}
