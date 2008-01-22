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
package functionalTests.component.requestpriority;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.AbstractProActiveController;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;


/**
 * @author cdalmass
 *
 */
public class NF3ItfImpl extends AbstractProActiveController implements NF3Itf {
    FItf me = null;

    public NF3ItfImpl(Component owner) {
        super(owner);
    }

    /*
     * (non-Javadoc)
     * 
     * @see functionalTests.component.requestpriority.NF3Itf#NF3Call()
     */
    public void NF3Call() {
        try {
            me = (FItf) owner.getFcInterface(FItf.ITF_NAME);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
        System.err.println("PriotirizedComponent:NF3Call");
        me.addCall(NF3Itf.NF3_STR_CALL);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(NF3Itf.CONTROLLER_NAME,
                    NF3Itf.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }
}
