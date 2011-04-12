package matlabcontrol;

import com.mathworks.jmi.Matlab;

/**
 * JMIWrapperImpl2
 *
 * @author The ProActive Team
 */
public class JMIWrapperImpl2 extends JMIWrapperImpl {

    public String eval(final String command) throws MatlabInvocationException {
        //return evalDeprecated(command);
        return evalDeprecated(command);
    }

    String evalDeprecated(final String command) throws MatlabInvocationException {
        if (instance == null) {
            instance = new Matlab();
        }
        try {
            return instance.eval(command);
        } catch (Exception e) {
            throw new MatlabInvocationException(MatlabInvocationException.INTERNAL_EXCEPTION_MSG, e);
        }
    }

//    String evalStreamOutput(final String command) throws MatlabInvocationException {
//
//        if (out == null) {
//            try {
//                FileOutputStream fos = new FileOutputStream(new File("/home/fviale/matlablog.txt"));
//                out = new PrintStream(new BufferedOutputStream(fos));
//                System.setErr(out);
//                System.setOut(out);
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//        getInstance().evalStreamOutput(command, new CompletionObserver() {
//            public void completed(int i, Object o) {
//                completed = true;
//                answer = o;
//                out.flush();
//                out.close();
//                out = null;
//            }
//        }, 1);
//        while (!completed) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return (String) answer;
//    }

     /**
     * Exits MATLAB.
     *
     * @throws MatlabInvocationException
     */
    public void exit(boolean immediate) throws MatlabInvocationException {
        if (!immediate) {
            Matlab.whenMatlabReady(new Runnable() {
                public void run() {
                    try {
                        Matlab.mtFevalConsoleOutput("exit", null, 0);
                    } catch (Exception e) {
                    }
                }
            });
        } else {
            getInstance().interrupt();
            getInstance().evalNoOutput("exit");
        }
    }
}
