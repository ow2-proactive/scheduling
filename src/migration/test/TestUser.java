package migration.test;

import org.objectweb.proactive.ProActive;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;

public class TestUser {

  public static void main(String[] args) throws java.lang.reflect.InvocationTargetException, FileNotFoundException, IOException, NotBoundException, NoSuchMethodException {
    UserAgent t = null;

    //	System.setSecurityManager(new RMISecurityManager());



    System.out.println("Testing UserAgent");
    System.out.println("Should migrate to zephir, arthur, oasis on Node1");
    System.out.println("****Creating Agent");
    //	UserAgent t = (UserAgent) Javall.newActiveAgentWithoutExceptions("MobileAgents.examples2.UserAgent",null,null);
    try {
      t = (UserAgent)ProActive.newActive("migration.test.UserAgent", null);

    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("******Creation done");
  }
}
