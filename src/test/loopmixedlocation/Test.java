package test.loopmixedlocation;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.ext.mixedlocation.MixedLocationMetaObjectFactory;

import java.io.Serializable;

public class Test implements Serializable {

  protected int index;
  protected String[] destinations;

  public Test() {}

  public Test(String[] nodes) {
    System.out.println("Test constructor with " + nodes.length + " destinations");
    index = 0;
    destinations = nodes;
  }

  public void live(Body body) {
    try {
      while (body.isActive()) {
        System.out.println("Test pending requests = " + body.getRequestQueue().size());
        while (!body.getRequestQueue().isEmpty()) {
          body.serve(body.getRequestQueue().removeOldest());
        }
        if (index < destinations.length) {
          index++;
          ProActive.migrateTo(destinations[index - 1]);
        } else {
          try {
            Thread.sleep(5000);
          } catch (Exception e) {
            e.printStackTrace();
          }
          System.out.println("Test pending requests = " + body.getRequestQueue().size());
          while (!body.getRequestQueue().isEmpty()) {
            body.serve(body.getRequestQueue().removeOldest());
          }
          index = 0;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void echo() {
    System.out.println("XXXXXXXXX  Test.echo  XXXXXXXXXXX");
  }

  public void migrateTo(String url) {
    try {
      ProActive.migrateTo(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java " + Test.class +" <destination>");
      System.exit(0);
    }
    Test t = null;
    Source s = null;
    Object[] arg = new Object[1];
    arg[0] = args;

    try {
      t = (Test) ProActive.newActive(Test.class.getName(), arg, MixedLocationMetaObjectFactory.newInstance(), null);
      s = (Source) ProActive.newActive(Source.class.getName(), null, MixedLocationMetaObjectFactory.newInstance(), null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    s.setTarget(t);
    s.start(args[0]);
  }

  public static class Source {

    protected Test t;
    protected String[] destinations;

    public Source() {}

    public Source(String[] s) {
      this.destinations = s;
    }

    public void setTarget(Test t) {
      this.t = t;
    }

    public void start(String destination) {
      int i = 0;
      while (true) {
        System.out.println("Call " + i++);
        t.echo();
        try {
          Thread.sleep(5000);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}