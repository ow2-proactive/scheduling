package org.objectweb.proactive.core.event;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;

/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RuntimeRegistrationEventProducerImpl
	extends AbstractEventProducer
	implements RuntimeRegistrationEventProducer
{

	

	//
	//-------------------implements RuntimeRegistrationEventProducer------------------
	//
	/**
	 * @see org.objectweb.proactive.core.runtime.event.RuntimeRegistrationEventProducer#addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
	 */
	public void addRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener){
		addListener(listener);
	}
	
	
	/**
	 * @see org.objectweb.proactive.core.runtime.event.RuntimeRegistrationEventProducer#removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener)
	 */
	public void removeRuntimeRegistrationEventListener(RuntimeRegistrationEventListener listener){
		removeListener(listener);
	}
	
	//
	//-------------------inherited methods from AbstractEventProducer------------------
	//
	/**
	 * @see org.objectweb.proactive.core.event.AbstractEventProducer#notifyOneListener(ProActiveListener, ProActiveEvent)
	 */
	protected void notifyOneListener(ProActiveListener proActiveListener, ProActiveEvent event){
  	RuntimeRegistrationEvent runtimeRegistrationEvent = (RuntimeRegistrationEvent) event;
  	RuntimeRegistrationEventListener runtimeRegistrationEventListener = (RuntimeRegistrationEventListener)proActiveListener;
  	//notify the listener that a registration occurs
  	runtimeRegistrationEventListener.runtimeRegistered(runtimeRegistrationEvent);
  }
  
  //
	//-------------------PROTECTED METHODS------------------
	//
  protected void notifyListeners(ProActiveRuntime proActiveRuntime,int type,String registeredRuntimeName, String creatorID, String protocol){
  	if (hasListeners()){
      notifyAllListeners(new RuntimeRegistrationEvent(proActiveRuntime, type, registeredRuntimeName, creatorID, protocol));
  	}
  	else System.out.println("no listener");
  }
}
