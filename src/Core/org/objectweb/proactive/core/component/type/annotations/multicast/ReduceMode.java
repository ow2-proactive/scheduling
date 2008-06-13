package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.component.exceptions.ReductionException;


public enum ReduceMode implements ReduceBehavior, Serializable {
    SELECT_UNIQUE_VALUE, CUSTOM;

    public Object reduce(List<?> values) throws ReductionException {
        switch (this) {
            case SELECT_UNIQUE_VALUE:
                if (!(values.size() == 1)) {
                    throw new ReductionException(
                        "invalid number of values to reduce: expected [1] but received [" + values.size() +
                            "]");
                }
                return values.iterator().next();

            default:
                return SELECT_UNIQUE_VALUE.reduce(values);
        }
    }

}
