package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;

/**
 * <p>
 * Event sent when a proActiveRuntime is registered in a Hashtable.
 * </p>
 * 
 * @author  ProActive Team
 * @version 1.0,  2002/08/06
 * @since   ProActive 0.9
 *
 */
public class RuntimeRegistrationEvent extends ProActiveEvent
{

	/** constant for the registration */
 	 public static final int RUNTIME_REGISTERED = 10;
	
	protected String creatorID;
	
	protected String registeredRuntimeName;
	
	protected String protocol;
	
  
  /**
   * Creates a new <code>RuntimeRegistrationEvent</code>
   * @param <code>proActiveRuntime</code> the local runtime on which the registration occurs
   * @param <code>messageType</code> the type of the event RUNTIME_REGISTERED
   * @param <code>registeredRuntimeName</code> the name of the registered ProActiveRuntime
   * @param <code>creatorID</code> The name of the creator of the registered ProActiveRuntime 
   * @param <code>protocol</code> The protocol used to register the registered ProActiveRuntime when created
   */
	public RuntimeRegistrationEvent(ProActiveRuntime ProActiveRuntime, int messagetype, String registeredRuntimeName, String creatorID ,String protocol)
	{
		super(ProActiveRuntime, messagetype);
		this.creatorID = creatorID;
		this.registeredRuntimeName = registeredRuntimeName;
		this.protocol = protocol;
	}
	
	
	public String getCreatorID(){
		return creatorID;
	}
	
	
	public String getRegisteredRuntimeName(){
		return registeredRuntimeName;
	}
	
	
	public String getProtocol(){
		return protocol;
	}

}
