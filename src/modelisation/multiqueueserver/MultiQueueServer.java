package modelisation.multiqueueserver;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ext.util.SimpleLocationServer;

public class MultiQueueServer extends SimpleLocationServer {

  public MultiQueueServer() {}

  public MultiQueueServer(String url) {
    super(url);
  }

  public void echo() {
    System.out.println("MultiQueueServer: echo()");
  }

  public void live(Body b) {
    System.out.println("Server is alive");

    super.live(b);
  }

  public static void main(String args[]) {
    if (args.length < 1) {
      System.out.println("usage: modelisation.MultiQueueServer <server url> [node]");
      System.exit(-1);
    }
    Object arg[] = new Object[1];
    arg[0] = args[0];
    MultiQueueServer server = null;
    try {
      if (args.length == 2)
        server = (MultiQueueServer) ProActive.newActive(MultiQueueServer.class.getName(), arg, CompositeQueueMetaObjectFactory.newInstance(), NodeFactory.getNode(args[1]));
      else
        server = (MultiQueueServer) ProActive.newActive(MultiQueueServer.class.getName(), arg, CompositeQueueMetaObjectFactory.newInstance(), null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}