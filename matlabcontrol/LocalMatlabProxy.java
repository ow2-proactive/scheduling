package matlabcontrol;

/*
 * Copyright (c) 2010, Joshua Kaplan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of matlabcontrol nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Allows for calling MATLAB from <b>within</b> MATLAB.
 * <br><br>
 * Methods may be called from any thread; however, calling from the Event
 * Dispatch Thread (EDT) used by AWT and Swing components can be problematic.
 * When a call is made the calling thread is paused. If the call into MATLAB
 * makes use of the EDT then MATLAB will hang indefinitely. The EDT is used
 * extensively by MATLAB when accessing graphical components such as a figure
 * window, uicontrols or plots.
 * <br><br>
 * The way methods are relayed to MATLAB differs depending on whether or not
 * the methods were invoked on the main MATLAB thread; unexpected behavior may
 * occur if methods are invoked from multiple threads. Any of the methods that
 * are relayed to MATLAB may throw exceptions. Exceptions may be thrown if an
 * internal MATLAB exception occurs.
 *
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
public final class LocalMatlabProxy {
    /**
     * The underlying wrapper to JMI.
     */
    private final static JMIWrapper _wrapper = new JMIWrapper();

    /**
     * Private constructor so that this class cannot be constructed.
     */
    private LocalMatlabProxy() {
    }

    /**
     * Exits MATLAB.
     *
     * @throws MatlabInvocationException
     */
    public static void exit() throws MatlabInvocationException {
        _wrapper.exit();
    }

    /**
     * Evaluates a command in MATLAB. The result of this command will not be
     * returned.
     * <br><br>
     * This is equivalent to MATLAB's <code>eval(['command'])</code>.
     *
     * @param command the command to be evaluated in MATLAB
     * @throws MatlabInvocationException
     * @see #returningEval(String, int)
     */
    public static void eval(String command) throws MatlabInvocationException {
        _wrapper.eval(command);
    }

    /**
     * Evaluates a command in MATLAB. The result of this command can be
     * returned.
     * <br><br>
     * This is equivalent to MATLAB's <code>eval(['command'])</code>.
     * <br><br>
     * In order for the result of this command to be returned the
     * number of arguments to be returned must be specified by
     * <code>returnCount</code>. If the command you are evaluating is a MATLAB
     * function you can determine the amount of arguments it returns by using
     * the <code>nargout</code> function in the MATLAB Command Window. If it
     * returns -1 that means the function returns a variable number of
     * arguments based on what you pass in. In that case, you will need to
     * manually determine the number of arguments returned. If the number of
     * arguments returned differs from <code>returnCount</code> then either
     * <code>null</code> or an empty <code>String</code> will be returned.
     *
     * @param command     the command to be evaluated in MATLAB
     * @param returnCount the number of arguments that will be returned from evaluating the command
     * @return result of MATLAB eval
     * @throws MatlabInvocationException
     * @see #eval(String)
     */
    public static Object returningEval(String command, int returnCount) throws MatlabInvocationException {
        return _wrapper.returningEval(command, returnCount);
    }

    /**
     * Calls a MATLAB function with the name <code>functionName</code>.
     * Arguments to the function may be provided as <code>args</code>, if you
     * wish to call the function with no arguments pass in <code>null</code>.
     * The result of this command will not be returned.
     * <br><br>
     * The <code>Object</code>s in the array will be converted into MATLAB
     * equivalents as appropriate. Importantly, this means that any
     * <code>String</code> will be converted to a MATLAB char array, not a
     * variable name.
     *
     * @param functionName name of the MATLAB function to call
     * @param args         the arguments to the function, <code>null</code> if none
     * @throws MatlabInvocationException
     * @see #returningFeval(String, Object[], int)
     * @see #returningFeval(String, Object[])
     */
    public static void feval(String functionName, Object[] args) throws MatlabInvocationException {
        _wrapper.feval(functionName, args);
    }

    /**
     * Calls a MATLAB function with the name <code>functionName</code>.
     * Arguments to the function may be provided as <code>args</code>, if you
     * wish to call the function with no arguments pass in <code>null</code>.
     * <br><br>
     * The <code>Object</code>s in the array will be converted into MATLAB
     * equivalents as appropriate. Importantly, this means that any
     * <code>String</code> will be converted to a MATLAB char array, not a
     * variable name.
     * <br><br>
     * The result of this function can be returned. In order for a function's
     * return data to be returned to MATLAB it is necessary to know how many
     * arguments will be returned. This method will attempt to determine that
     * automatically, but in the case where a function has a variable number of
     * arguments returned it will only return one of them. To have all of them
     * returned use {@link #returningFeval(String, Object[], int)} and specify
     * the number of arguments that will be returned.
     *
     * @param functionName name of the MATLAB function to call
     * @param args         the arguments to the function, <code>null</code> if none
     * @return result of MATLAB function
     * @throws MatlabInvocationException
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     */
    public static Object returningFeval(String functionName, Object[] args) throws MatlabInvocationException {
        return _wrapper.returningFeval(functionName, args);
    }

    /**
     * Calls a MATLAB function with the name <code>functionName</code>.
     * Arguments to the function may be provided as <code>args</code>, if you
     * wish to call the function with no arguments pass in <code>null</code>.
     * <br><br>
     * The <code>Object</code>s in the array will be converted into MATLAB
     * equivalents as appropriate. Importantly, this means that any
     * <code>String</code> will be converted to a MATLAB char array, not a
     * variable name.
     * <br><br>
     * The result of this function can be returned. In order for the result of
     * this function to be returned the number of arguments to be returned must
     * be specified by <code>returnCount</code>. You can use the
     * <code>nargout</code> function in the MATLAB Command Window to determine
     * the number of arguments that will be returned. If <code>nargout</code>
     * returns -1 that means the function returns a variable number of
     * arguments based on what you pass in. In that case, you will need to
     * manually determine the number of arguments returned. If the number of
     * arguments returned differs from <code>returnCount</code> then either
     * only some of the items will be returned or <code>null</code> will be
     * returned.
     *
     * @param functionName name of the MATLAB function to call
     * @param args         the arguments to the function, <code>null</code> if none
     * @param returnCount  the number of arguments that will be returned from this function
     * @return result of MATLAB function
     * @throws MatlabInvocationException
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     */
    public static Object returningFeval(String functionName, Object[] args, int returnCount) throws MatlabInvocationException {
        return _wrapper.returningFeval(functionName, args, returnCount);
    }

    /**
     * Sets the variable to the given <code>value</code>.
     *
     * @param variableName
     * @param value
     * @throws MatlabInvocationException
     */
    public static void setVariable(String variableName, Object value) throws MatlabInvocationException {
        _wrapper.setVariable(variableName, value);
    }

    /**
     * Gets the value of the variable named </code>variableName</code> from MATLAB.
     *
     * @param variableName
     * @return value
     * @throws MatlabInvocationException
     */
    public static Object getVariable(String variableName) throws MatlabInvocationException {
        return _wrapper.getVariable(variableName);
    }

    /**
     * Allows for enabling a diagnostic mode that will show in MATLAB each time
     * a Java method that calls into MATLAB is invoked.
     *
     * @param echo
     * @throws MatlabInvocationException
     */
    public static void setEchoEval(boolean echo) throws MatlabInvocationException {
        _wrapper.setEchoEval(echo);
    }
}