package test.asynchronouscreation;

import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ProActive;

import java.util.Random;

public class Test {

  private Integer[] tablo;


  public Test() {

  }


  public Test(Integer[] tablo) {
    //	this.tablo = tablo;
    System.out.println("Creation done");

  }


  public Test createTestObject(Object[] arg, String dest) {
    Test tmp = null;
    try {
      tmp = (Test)ProActive.newActive("test.asynchronouscreation.Test", arg, NodeFactory.getNode(dest));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tmp;
  }


  public static void testSynchrone(Object[] arg, int number, String dest) {

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < number; i++) {
      //	System.out.println("arg = " + arg);
 
      try {
        ProActive.newActive("test.asynchronouscreation.Test", arg, NodeFactory.getNode(dest));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    long time = System.currentTimeMillis() - startTime;
    System.err.println("XXXXXXX Test: creation time for " + number + " objects is " + time);
  }


  public static void testAsynchrone(Object[] arg, int number, String dest) {
    //Asynchronous creation
    Test creator = null;
    try {
      creator = (Test)ProActive.newActive("test.asynchronouscreation.Test", null, NodeFactory.getNode("//tuba/essai"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < number; i++) {
      creator.createTestObject(arg, dest);
    }
    long time = System.currentTimeMillis() - startTime;
    System.err.println("XXXXXXX Test: asynchronous creation time for " + number + " objects is " + time);
  }


  public static void main(String[] args) {
    if (args.length < 3) {
      System.err.println("Usage asynchronouscreation.Test testValue <number> <nodeName>");
      System.err.println("   testValue = 'sync' or 'async' or 'both'");
      System.exit(-1);
    }

    Random random = new Random();
    Integer[] dataArray = new Integer[10000];
    for (int j = 0; j < 10000; j++) {
      dataArray[j] = new Integer(random.nextInt());
    }

    int number = Integer.parseInt(args[1]);

    Object param[] = new Object[1];
    param[0] = dataArray;

    if ((args[0].equals("both")) || (args[0].equals("sync"))) {
      Test.testSynchrone(param, number, args[2]);

    }
    if (args[0].equals("both")) {
      try {
        Thread.sleep(5000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if ((args[0].equals("both")) || (args[0].equals("async")))
      Test.testAsynchrone(param, number, args[2]);

  }
}
