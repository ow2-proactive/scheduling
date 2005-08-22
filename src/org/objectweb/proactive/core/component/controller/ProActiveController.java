package org.objectweb.proactive.core.component.controller;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;


public interface ProActiveController {

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfOwner()}
     */
    public abstract Component getFcItfOwner();

    /*
     * see {@link org.objectweb.fractal.api.Interface#isFcInternalItf()}
     */
    public abstract boolean isFcInternalItf();

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    public abstract String getFcItfName();

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfType()}
     */
    public abstract Type getFcItfType();
}
