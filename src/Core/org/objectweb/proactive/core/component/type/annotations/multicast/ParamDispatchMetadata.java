/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying the dispatch strategy of a given parameter.
 *
 * @author Matthieu Morel
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ParamDispatchMetadata {
    /**
     * Selection of dispatch strategy from the {@link ParamDispatchMode} enumeration.
     * @return selected dispatch strategy
     */
    ParamDispatchMode mode();
    /**
     * Used for specifying a custom dispatch strategy. Custom dispatch strategies are defined in classes that
     * implement the {@link ParamDispatch} interface.
     * <br>
     * For a custom dispatch strategy to be specified, the ParamDispatchMode.CUSTOM value must be selected for
     * the {@link ParamDispatchMetadata#mode()} method.
     * @return a class defining the dispatch strategy
     */
    Class<?> customMode() default ParamDispatchMode.class;
}
