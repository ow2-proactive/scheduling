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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.descriptor.legacyparser;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;


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
    public InfrastructureHandler(
        ProActiveDescriptorInternal proActiveDescriptor) {
        super(false);
        CollectionUnmarshaller ch = new CollectionUnmarshaller();
        ch.addHandler(PROCESS_DEFINITION_TAG,
            new ProcessDefinitionHandler(proActiveDescriptor));
        this.addHandler(PROCESSES_TAG, ch);
        ch.addHandler(SERVICE_DEFINITION_TAG,
            new ServiceDefinitionHandler(proActiveDescriptor));
        this.addHandler(SERVICES_TAG, ch);
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
