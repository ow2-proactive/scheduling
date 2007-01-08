package org.objectweb.proactive.examples.pi;

import java.util.List;

import org.objectweb.proactive.core.component.type.annotations.multicast.ClassDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMetadata;
import org.objectweb.proactive.core.component.type.annotations.multicast.ParamDispatchMode;



/**
 * This interface represents the client multicast interface of the master component in the component version of the application.
 * The dispatch mode is one to one. That means that the parameters of the methods are scattered. If a parameter is a list, each item of the list sent to one component the interface is bound to.
 * It also means that the number of elements in the list and the number of components the interface is bound to have to be the same.
 * @author Paul Naoumenko
 *
 */
@ClassDispatchMetadata(
		mode=@ParamDispatchMetadata(mode=ParamDispatchMode.ONE_TO_ONE))
public interface PiCompMultiCast {
	
	/**
	 * Initiates the computation of pi on all the workers bound to the client multicast interface
	 * @param msg the list of intervals that have to be distributed to the workers
	 * @return The list of partial results of pi computation that have to be gathered into the final pi value
	 */
	public List<Result> compute(List<Interval> msg);
	
	/**
	 * Sets scale for several pi computers
	 * @param scale The scale to set
	 */
	public void setScale(List<Integer> scale);

}
