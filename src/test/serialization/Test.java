package test.serialization;

import org.objectweb.proactive.Active;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;

public class Test implements Active, Serializable {

  Vector v = new Vector();
  transient java.io.ObjectOutputStream oos;
  transient java.io.ByteArrayOutputStream baos;


  public Test() {
  }


  public void live(Body body) {
    //TEMPORARY
    long startTime = System.currentTimeMillis();
    long startTime2 = 0;
    try {
      baos = new java.io.ByteArrayOutputStream();
      oos = new java.io.ObjectOutputStream(baos);

      for (int i = 0; i < 5000; i++) {
        if (i == 0) {
          startTime2 = System.currentTimeMillis();
        }
        //System.out.println("toto");
 
        oos.writeObject(body);
        oos.reset();
        if (i == 0) {
          long endTime2 = System.currentTimeMillis() - startTime2;
          System.out.println(" First serialization lasted " + endTime2);
          System.out.println("The size of the ByteArrayOutputStream is " + baos.size());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    long endTime = System.currentTimeMillis() - startTime;
    System.out.println("serialization over, lasted " + endTime);
    System.out.println(" average value " + (endTime / 5000));

    try {
      byte[] serializedObjects = baos.toByteArray();

      ByteArrayInputStream bais = new java.io.ByteArrayInputStream(serializedObjects);
      ObjectInputStream ois = new java.io.ObjectInputStream(bais);

      startTime = System.currentTimeMillis();
      ois.readObject();
      endTime = System.currentTimeMillis() - startTime;
      System.out.println(" The deserialization lasted " + endTime);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  public static void main(String[] args) {
    Test test = null;
    try {
      test = (Test)ProActive.newActive("test.serialization.Test", null);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
