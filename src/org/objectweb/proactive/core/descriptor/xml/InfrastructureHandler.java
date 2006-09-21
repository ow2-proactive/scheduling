/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.scheduler.Scheduler;


/**
 * This class receives deployment events
 *
 * @author       Lionel Mestre
 * @version      1.0
 */
class InfrastructureHandler extends PassiveCompositeUnmarshaller
    implements ProActiveDescriptorConstants {
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //
    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //
    public InfrastructureHandler(ProActiveDescriptor proActiveDescriptor) {
        super(false);
        CollectionUnmarshaller ch = new CollectionUnmarshaller();
        ch.addHandler(PROCESS_DEFINITION_TAG,
            new ProcessDefinitionHandler(proActiveDescriptor));
        this.addHandler(PROCESSES_TAG, ch);
        ch.addHandler(SERVICE_DEFINITION_TAG,
            new ServiceDefinitionHandler(proActiveDescriptor));
        this.addHandler(SERVICES_TAG, ch);
    }

    public InfrastructureHandler(Scheduler scheduler, String jobId,
        ProActiveDescriptor proActiveDescriptor) {
        super(false);
        CollectionUnmarshaller ch = new CollectionUnmarshaller();
        ch.addHandler(SERVICE_DEFINITION_TAG,
            new ServiceDefinitionHandler(scheduler, jobId, proActiveDescriptor));
        this.addHandler(SERVICES_TAG, ch);
        ch.addHandler(PROCESS_DEFINITION_TAG,
            new ProcessDefinitionHandler(proActiveDescriptor));
        this.addHandler(PROCESSES_TAG, ch);
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    //
    //  ----- PROTECTED METHODS -----------------------------------------------------------------------------------
    //
    //
    //  ----- INNER CLASSES -----------------------------------------------------------------------------------
    //
}
