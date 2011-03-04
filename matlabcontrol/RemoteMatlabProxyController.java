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
 * <b>This class is deprecated. It will be removed in a later release of matlabcontrol.
 * Use of this class is strongly discouraged.</b>
 * <br><br>
 * A convenience class that allows for controlling a single remote session of MATLAB
 * without having to create a {@link RemoteMatlabProxyFactory} and then create a
 * {@link RemoteMatlabProxy}.
 * 
 * @see RemoteMatlabProxy
 * 
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
@Deprecated
public class RemoteMatlabProxyController
{
	/**
	 * The factory used to create the proxy.
	 */
	private static RemoteMatlabProxyFactory _factory;
	
	/**
	 * The proxy which the static methods are being relayed to.
	 */
	private static RemoteMatlabProxy _proxy;
	
	/**
	 * This class cannot be constructed, all methods of use are static.
	 */
	private RemoteMatlabProxyController() {}
	
	/**
	 * Sets the variable to the given <code>value</code>.
	 * 
	 * @param variableName
	 * @param value
	 * 
	 * @throws MatlabInvocationException
	 */
	public static void setVariable(String variableName, Object value) throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			_proxy.setVariable(variableName, value);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
	}
	
	/**
	 * Gets the value of the variable named </code>variableName</code> from MATLAB.
	 * 
	 * @param variableName
	 * 
	 * @return value
	 * 
	 * @throws MatlabInvocationException
	 */
	public static Object getVariable(String variableName) throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			return _proxy.getVariable(variableName);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
	}
	
	/**
	 * Create a connection to MATLAB. If a connection already exists this
	 * method will not do anything. This must be called before any methods
	 * that control MATLAB are called, or those methods will throw runtime
	 * exceptions.
	 * 
	 * @throws MatlabConnectionException
	 */
	public static void createConnection() throws MatlabConnectionException
	{
		if(!RemoteMatlabProxyController.isConnected())
		{
			_factory = new RemoteMatlabProxyFactory();
			
			_factory.addConnectionListener(new MatlabConnectionListener()
			{
				public void connectionEstablished(RemoteMatlabProxy proxy)
				{
					_proxy = proxy;
				}
	
				public void connectionLost(RemoteMatlabProxy proxy)
				{
					_proxy = null;
				}
			});
			
			_proxy = _factory.getProxy();
		}
	}
	
	/**
	 * Returns whether or not this controller is connected to MATLAB.
	 *
	 * @return if connected to MATLAB
	 */
	public static boolean isConnected()
	{
		return (_proxy != null && _proxy.isConnected());
	}
	
	/**
	 * Exits MATLAB.
	 * 
	 * @throws MatlabInvocationException 
	 */
	public static void exit() throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			_proxy.exit();
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
	}
	
    /**
     * Evaluates a command in MATLAB. The result of this command will not be
     * returned.
     * <br><br>
     * This is equivalent to MATLAB's <code>eval(['command'])</code>.
     * 
     * @param command the command to be evaluated in MATLAB
     * @throws MatlabInvocationException 
     * 
     * @see #returningEval(String, int)
     */
	public static void eval(String command) throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			_proxy.eval(command);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
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
     * @param command the command to be evaluated in MATLAB
     * @param returnCount the number of arguments that will be returned from evaluating the command
     * 
     * @see #eval(String)
     * 
     * @return result of MATLAB eval
     * @throws MatlabInvocationException 
     */
	public static Object returningEval(String command, int returnCount) throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			return _proxy.returningEval(command, returnCount);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
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
     * @param args the arguments to the function, <code>null</code> if none
     * @throws MatlabInvocationException 
     * 
     * @see #returningFeval(String, Object[], int)
     * @see #returningFeval(String, Object[])
     */
	public static void feval(String functionName, Object[] args) throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			_proxy.feval(functionName, args);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
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
     * @param args the arguments to the function, <code>null</code> if none
     * 
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     * 
     * @return result of MATLAB function
     * @throws MatlabInvocationException 
     */
	public static Object returningFeval(String functionName, Object[] args) throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			return _proxy.returningFeval(functionName, args);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
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
     * @param args the arguments to the function, <code>null</code> if none
     * @param returnCount the number of arguments that will be returned from this function
     * 
     * @see #feval(String, Object[])
     * @see #returningFeval(String, Object[])
     * 
     * @return result of MATLAB function
     * @throws MatlabInvocationException 
     */
	public static Object returningFeval(String functionName, Object[] args, int returnCount) throws MatlabInvocationException
	{
		if(RemoteMatlabProxyController.isConnected())
		{
			return _proxy.returningFeval(functionName, args, returnCount);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
	}
	
    /**
     * Allows for enabling a diagnostic mode that will show in MATLAB each time
     * a Java method that calls into MATLAB is invoked.
     * 
     * @param echo
     * @throws MatlabInvocationException 
     */
	public static void setEchoEval(boolean echo) throws MatlabInvocationException
	{		
		if(RemoteMatlabProxyController.isConnected())
		{
			_proxy.setEchoEval(echo);
		}
		else
		{
			throw new MatlabInvocationException(MatlabInvocationException.CONTROLLER_NO_CONNECTION_MSG);
		}
	}
}