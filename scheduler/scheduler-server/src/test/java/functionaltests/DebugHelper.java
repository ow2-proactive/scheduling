package functionaltests;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.tools.jdi.SocketAttachingConnector;


public class DebugHelper {

    private final int debugPort;

    private VirtualMachine vm;

    public DebugHelper() throws Exception {
        ServerSocket socket = new ServerSocket(0);
        debugPort = socket.getLocalPort();
        socket.close();
    }

    public List<String> getDebuggedVMOptions() {
        String listenerOption = String.format(
                "-agentlib:jdwp=transport=dt_socket,server=y,address=%d,suspend=n", debugPort);
        List<String> result = new ArrayList<String>();
        result.add(listenerOption);
        return result;
    }

    @SuppressWarnings("unchecked")
    public void connect() throws Exception {
        SocketAttachingConnector socketConnector = null;
        for (Connector connector : Bootstrap.virtualMachineManager().allConnectors()) {
            if (connector instanceof SocketAttachingConnector) {
                socketConnector = (SocketAttachingConnector) connector;
            }
        }
        if (socketConnector == null) {
            throw new RuntimeException("Failed to find SocketAttachingConnector");
        }

        Map<String, ? extends Connector.Argument> args = socketConnector.defaultArguments();
        Connector.IntegerArgument port = (Connector.IntegerArgument) args.get("port");
        port.setValue(debugPort);

        vm = socketConnector.attach(args);
    }

    public void disconnect() {
        if (vm != null) {
            vm.dispose();
            vm = null;
        }
    }

    public void suspendVM() {
        checkConnected();
        vm.suspend();
    }

    public void resumeVM() {
        checkConnected();
        vm.resume();
    }

    private void checkConnected() {
        if (vm == null) {
            throw new IllegalStateException("Not connected");
        }
    }
}
