package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ReductionException;


public interface ReduceBehavior {

    public Object reduce(List<?> values) throws ReductionException;

}
