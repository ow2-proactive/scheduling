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
package org.objectweb.proactive.extensions.scilab.monitor;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scilab.SciEngine;


/**
 * SciEngineInfo contains all methods to access to informations about a Scilab Engine
 */
public class SciEngineInfo {
    private String idEngine;
    private String idCurrentTask;
    private SciEngine sciEngine;
    private BooleanWrapper isActivate; //a future to test if the Scilab engine is activated

    public SciEngineInfo(String idEngine, SciEngine sciEngine,
        BooleanWrapper isActivate) {
        this.idEngine = idEngine;
        this.sciEngine = sciEngine;
        this.isActivate = isActivate;
    }

    public String getIdEngine() {
        return idEngine;
    }

    public SciEngine getSciEngine() {
        return sciEngine;
    }

    public String getSciEngineUrl() {
        return ProActiveObject.getActiveObjectNodeUrl(this.sciEngine);
    }

    public BooleanWrapper getIsActivate() {
        return isActivate;
    }

    public void setIsActivate(BooleanWrapper isActivate) {
        this.isActivate = isActivate;
    }

    public String getIdCurrentTask() {
        return idCurrentTask;
    }

    public void setIdCurrentTask(String idCurrentTask) {
        this.idCurrentTask = idCurrentTask;
    }
}
