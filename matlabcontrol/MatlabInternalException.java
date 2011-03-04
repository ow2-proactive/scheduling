package matlabcontrol;

import com.mathworks.jmi.MatlabException;

/**
 * A wrapper around com.mathworks.jmi.MatlabException so that the exception
 * can be sent over RMI without needing the jmi.jar to be included by the
 * developer, but still prints identically.
 * 
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
class MatlabInternalException extends Exception
{	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The <code>String</code> representation of the <code>MatlabException</code>
	 * so that this exception can pretend to be a <code>MatlabException</code>
	 */
	private final String _toString;

	/**
	 * Creates a wrapper around <code>innerException</code> so that
	 * when the stack trace is printed it is the same to the developer,
	 * but can be easily sent over RMI.
	 * 
	 * @param innerException
	 */
	MatlabInternalException(MatlabException innerException)
	{
		//Store innerException's toString() value
		_toString = innerException.toString();
		
		//Set this stack trace to that of the innerException
		this.setStackTrace(innerException.getStackTrace());
	}
	
	@Override
	public String toString()
	{
		return _toString;
	}
}