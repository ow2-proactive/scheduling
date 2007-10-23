package org.objectweb.proactive.extra.scheduler.ext.matlab;

import java.util.ArrayList;

import org.objectweb.proactive.extra.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extra.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.objectweb.proactive.extra.scheduler.ext.matlab.exception.InvalidParameterException;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;


public class AOMatlabCollector extends AOSimpleMatlab {

    /**
         *
         */
    private static final long serialVersionUID = -5140257280630144580L;

    public AOMatlabCollector() {
    }

    /**
     * Constructor for the Collector task
     * @param matlabCommandName the name of the Matlab command
     * @param inputScript  a pre-matlab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     */
    public AOMatlabCollector(String matlabCommandName, String inputScript,
        ArrayList<String> scriptLines) {
        super(matlabCommandName, inputScript, scriptLines);
    }

    public Object execute(TaskResult... results) throws Throwable {
        if (results.length <= 0) {
            throw new InvalidNumberOfParametersException(results.length);
        }

        Token[] tokens = new Token[results.length];
        for (int i = 0; i < results.length; i++) {
            TaskResult res = results[i];
            if (res.hadException()) {
                throw res.getException();
            }
            if (!(res.value() instanceof Token)) {
                throw new InvalidParameterException(res.getClass());
            }
            Token token = (Token) res.value();
            if (i > 0) {
                if (!tokens[i - 1].getType().equals(token.getType())) {
                    throw new InvalidParameterException(token.getType(),
                        tokens[i - 1].getType());
                }
            }
            tokens[i] = token;
        }
        ArrayToken collectArray = new ArrayToken(tokens);
        MatlabEngine.put("in", collectArray);
        executeScript();
        return MatlabEngine.get("out");
    }
}
