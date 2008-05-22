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
package functionalTests.component.nonfunctional.creation;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 *
 * @author The ProActive Team
 *
 * Content class of the dummy controller component
 */
public class DummyMaster implements DummyControllerItf, BindingController {
    private DummyControllerItf dummyController;

    public String dummyMethodWithResult() {
        return dummyController.dummyMethodWithResult();
    }

    public void dummyVoidMethod(String message) {
        dummyController.dummyVoidMethod(message);
    }

    public void bindFc(String arg0, Object arg1) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (arg0.equals("dummy-client")) {
            dummyController = (DummyControllerItf) arg1;
        }
    }

    public String[] listFc() {
        return new String[] { "dummy-client" };
    }

    public Object lookupFc(String arg0) throws NoSuchInterfaceException {
        if (arg0.equals("dummy-client")) {
            return dummyController;
        }
        return null;
    }

    public void unbindFc(String arg0) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (arg0.equals("dummy-client")) {
            dummyController = null;
        }
    }

    public IntWrapper result(IntWrapper param) {

        return dummyController.result(param);
    }
}
