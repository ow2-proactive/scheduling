package org.objectweb.proactive.extra.scheduler.ext.matlab.exception;

import ptolemy.data.type.Type;


public class UncompatibleTypeMergeException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 8838179202567335739L;

    public UncompatibleTypeMergeException(String key, Type type, Type type2) {
        super("Impossible to merge types from variable \"" + key +
            "\", types are " + type.toString() + " and " + type.toString());
    }
}
