package modelisation.multiqueueserver;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.ext.util.SimpleLocationServer;

public class MultiQueueServer extends SimpleLocationServer implements org.objectweb.proactive.RunActive {

  public MultiQueueServer() {}

  public MultiQueueServer(String url) {
    super(url);
  }

  public void echo() {
    System.out.println("MultiQueueServer: echo()");
  }

  public void runActivity(Body b) {
    System.out.println("Server is alive");
    super.runActivity(b);
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
        server = (MultiQueueServer) ProActive.newActive(MultiQueueServer.class.getName(), arg, 
                      NodeFactory.getNode(args[1]),
                      null,
                      CompositeQueueMetaObjectFactory.newInstance());
      else
        server = (MultiQueueServer) ProActive.newActive(MultiQueueServer.class.getName(), arg, 
                      null, null, CompositeQueueMetaObjectFactory.newInstance());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}