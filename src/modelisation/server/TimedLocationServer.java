package modelisation.server;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.timer.MicroTimer;
import org.objectweb.proactive.ext.util.SimpleLocationServer;

public class TimedLocationServer extends SimpleLocationServer implements org.objectweb.proactive.RunActive {

    protected MicroTimer microtimer;

    public TimedLocationServer() {
    }


    public TimedLocationServer(String url) {
        super(url);
        this.microtimer = new MicroTimer();
    }

    public void runActivity(Body body) {
        System.out.println("TimedLocationServer: live()");
        this.register();
        while (body.isActive()) {
            try {
                Request request = body.getRequestQueue().blockingRemoveOldest();
                Reply reply = null;
                microtimer.start();
                reply = request.serve(body);
                this.processReply(body, request, reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void processReply(Body body, Request request, Reply reply) {
        if (reply != null) {
            try {
                reply.send(request.getSender());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        microtimer.stop();
        System.out.println("SelectiveServer: time microtimer = "
                           + microtimer.getCumulatedTime() + " for method "
                           + request.getMethodName());
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("usage: modelisation.SelectiveServer <server url> [node]");
            System.exit(-1);
        }
        Object arg[] = new Object[1];
        arg[0] = args[0];
        TimedLocationServer server = null;
        try {
            if (args.length == 2)
                server = (TimedLocationServer)
                        ProActive.newActive(TimedLocationServer.class.getName(),
                                            arg, NodeFactory.getNode(args[1]));
            else
                server = (TimedLocationServer)
                        ProActive.newActive(TimedLocationServer.class.getName(),
                                            arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
