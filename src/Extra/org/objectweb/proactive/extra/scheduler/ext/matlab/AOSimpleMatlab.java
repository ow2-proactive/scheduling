package org.objectweb.proactive.extra.scheduler.ext.matlab;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.objectweb.proactive.extra.scheduler.ext.matlab.exception.InvalidParameterException;

import ptolemy.data.Token;


public class AOSimpleMatlab implements Serializable {
    private String inputScript = null;
    private ArrayList<String> scriptLines = new ArrayList<String>();

    public AOSimpleMatlab() {
    }

    /**
     * Constructor for the Simple task
     * @param matlabCommandName the name of the Matlab command
     * @param inputScript  a pre-matlab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     */
    public AOSimpleMatlab(String matlabCommandName, String inputScript,
        ArrayList<String> scriptLines) {
        this.inputScript = inputScript;
        this.scriptLines = scriptLines;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    MatlabEngine.close();
                }
            }));
        MatlabEngine.setCommandName(matlabCommandName);
    }

    public Object execute(int index, TaskResult... results)
        throws Throwable {
        if (results.length > 1) {
            throw new InvalidNumberOfParametersException(results.length);
        }
        if (results.length == 1) {
            TaskResult res = results[0];
            if (index != -1) {
                if (!(res.value() instanceof SplittedResult)) {
                    throw new InvalidParameterException(res.value().getClass());
                }
                SplittedResult sr = (SplittedResult) res.value();
                Token tok = sr.getResult(index);
                MatlabEngine.put("in", tok);
            } else {
                if (!(res.value() instanceof Token)) {
                    throw new InvalidParameterException(res.value().getClass());
                }
                Token in = (Token) res.value();
                MatlabEngine.put("in", in);
            }
        }
        executeScript();
        return MatlabEngine.get("out");
    }

    /**
     * Terminates the Matlab engine
     * @return true for synchronous call
     */
    public boolean terminate() {
        MatlabEngine.close();
        return true;
    }

    /**
     * Executes both input and main scripts on the engine
     * @throws Throwable
     */
    protected final void executeScript() throws Throwable {
        if (inputScript != null) {
            System.out.println("Feeding input");
            MatlabEngine.evalString(inputScript);
        }
        String execScript = prepareScript();
        System.out.println("Executing Script");
        MatlabEngine.evalString(execScript);
    }

    /**
     * Appends all the script's lines as a single string
     * @return
     */
    private String prepareScript() {
        String script = "";

        for (String line : scriptLines) {
            script += line;
            script += nl;
        }
        return script;
    }

    /**
         *
         */
    private static final long serialVersionUID = -933248120525832351L;
    static String nl = System.getProperty("line.separator");
}
