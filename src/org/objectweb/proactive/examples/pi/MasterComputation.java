package org.objectweb.proactive.examples.pi;
import java.util.List;
/**
 * The server interface of the master component for the component version of this example
 * @author Paul Naoumenko
 *
 */
public interface MasterComputation {
	public boolean computePi(List<Interval> params);

}
