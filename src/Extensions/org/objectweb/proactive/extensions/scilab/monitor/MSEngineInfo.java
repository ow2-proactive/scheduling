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
package org.objectweb.proactive.extensions.scilab.monitor;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.scilab.MSEngine;


/**
 * MSEngineInfo contains all methods to access to informations about a Scilab Engine
 */
public class MSEngineInfo {
    private String idEngine;
    private String idCurrentTask;
    private MSEngine mSEngine;
    private BooleanWrapper isActivate; //a future to test if the Scilab engine is activated

    public MSEngineInfo(String idEngine, MSEngine mSEngine, BooleanWrapper isActivate) {
        this.idEngine = idEngine;
        this.mSEngine = mSEngine;
        this.isActivate = isActivate;
    }

    public String getIdEngine() {
        return idEngine;
    }

    public MSEngine getMSEngine() {
        return mSEngine;
    }

    public String getSciEngineUrl() {
        return PAActiveObject.getActiveObjectNodeUrl(this.mSEngine);
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
