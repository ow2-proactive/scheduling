package org.objectweb.proactive.core.component.type;

import org.objectweb.fractal.api.type.InterfaceType;

public interface ProActiveInterfaceType extends InterfaceType {
    
    /**
     * Returns the cardinality of this interface. The possible cardinalities are :
     * <ul>
     *  <li> {@link ProActiveTypeFactory#SINGLETON_CARDINALITY singleton}</li>
     *  <li> {@link ProActiveTypeFactory#COLLECTION_CARDINALITY collection}</li>
     *  <li> {@link ProActiveTypeFactory#MULTICAST_CARDINALITY multicast}</li>
     *  <li> {@link ProActiveTypeFactory#GATHER_CARDINALITY gathercast}</li>
     *  </ul>
     *
     * @return the cardinality of the interface
     */
    public String getFcCardinality();
    
    public boolean isFcSingletonItf();
    
    public boolean isFcMulticastItf();
    
    public boolean isFcGathercastItf();
    
    

}
