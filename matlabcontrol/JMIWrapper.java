package matlabcontrol;

/**
 * JMIWrapper
 *
 * @author The ProActive Team
 */
public interface JMIWrapper {
    void setVariable(String variableName, Object value) throws MatlabInvocationException;

    Object getVariable(String variableName) throws MatlabInvocationException;

    void waitReady();

    void exit(boolean immediate) throws MatlabInvocationException;

    String eval(String command) throws MatlabInvocationException;

    Object returningEval(String command, int returnCount) throws MatlabInvocationException;

    void feval(String functionName, Object[] args) throws MatlabInvocationException;

    Object returningFeval(String functionName, Object[] args, int returnCount) throws MatlabInvocationException;

    Object returningFeval(String functionName, Object[] args) throws MatlabInvocationException;

    void setEchoEval(boolean echo);
}
