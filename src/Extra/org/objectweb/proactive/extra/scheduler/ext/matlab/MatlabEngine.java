package org.objectweb.proactive.extra.scheduler.ext.matlab;

import java.io.PrintWriter;
import java.io.StringWriter;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.Engine;


public class MatlabEngine {
    static Engine eng = null;
    static long[] engineHandle;
    static String commandName;

    static void init() throws IllegalActionException {
        if (eng == null) {
            try {
                eng = new Engine();

                eng.setDebugging((byte) 0);

                engineHandle = eng.open(commandName +
                        " -nodisplay -nosplash -nodesktop", true);
            } catch (UnsatisfiedLinkError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the Matlab libraries");
                pw.println("PATH=" + System.getenv("PATH"));
                pw.println("LD_LIBRARY_PATH=" +
                    System.getenv("LD_LIBRARY_PATH"));
                pw.println("java.library.path=" +
                    System.getProperty("java.library.path"));

                UnsatisfiedLinkError ne = new UnsatisfiedLinkError(error_message.toString());
                ne.initCause(e);
                throw ne;
            } catch (NoClassDefFoundError e) {
                StringWriter error_message = new StringWriter();
                PrintWriter pw = new PrintWriter(error_message);
                pw.println("Can't find the ptolemy classes");
                pw.println("java.class.path=" +
                    System.getProperty("java.class.path"));
                NoClassDefFoundError ne = new NoClassDefFoundError(error_message.toString());
                ne.initCause(e);
                throw ne;
            }
        }
    }

    public static void setCommandName(String name) {
        commandName = name;
    }

    public static String getCommandName() {
        return commandName;
    }

    public static void evalString(String command) throws IllegalActionException {
        init();
        eng.evalString(engineHandle, command);
    }

    public static Token get(String variableName) throws IllegalActionException {
        init();
        return eng.get(engineHandle, variableName);
    }

    public static void put(String variableName, Token token)
        throws IllegalActionException {
        init();
        eng.put(engineHandle, variableName, token);
    }

    public static void close() {
        eng.close(engineHandle);
        eng = null;
    }
}
