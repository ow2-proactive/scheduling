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
package org.objectweb.proactive.core.component.interception;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * This interface must be implemented by controllers that need to intercept
 * outgoing functional invocations. <br>
 * Before executing (in the case of a primitive component) or transferring (in
 * the case of a composite component) an outgoing functional request, the
 * <code> beforeOutputMethodInvocation  </code> method is called, and the
 * <code> afterOutputMethodInvocation  </code> is called after the execution or
 * transfer of the invocation. <br>
 * These methods are executed on the controllers of the current component that
 * implement this interface. <br>
 * The <code>beforeOutputMethodInvocation </code> method is called sequentially
 * for each controller in the order they are defined in the controllers
 * configuration file. <br>
 * The <code>afterOutputMethodInvocation</code> method is called sequentially
 * for each controller in the <b> reverse order </b> they are defined in the
 * controllers configuration file. <br>
 * Example : <br>
 * if in the controller config file, the list of input interceptors is in this
 * order (the order in the controller config file is from top to bottom) : <br>
 * <code> OutputInterceptor1  </code> <br>
 * <code> OutputInterceptor2  </code> <br>
 * This means that an invocation on a server interface will follow this path :
 * <br>
 * <code> --> currentComponent  </code> <br>
 * <code> --> OutputInterceptor1.beforeOutputMethodInvocation  </code> <br>
 * <code> --> OutputInterceptor2.beforeOutputMethodInvocation  </code> <br>
 * <code> --> callee.invocation  </code> <br>
 * <code> --> OutputInterceptor2.afterOutputMethodInvocation  </code> <br>
 * <code> --> OutputInterceptor1.afterOutputMethodInvocation  </code>
 *
 * @author Matthieu Morel
 */
@PublicAPI
public interface OutputInterceptor {

    /**
     * This method is executed when an output invocation is intercepted, before executing the output invocation.
     * @param methodCall the method to be executed (MethodCall objects include method parameters and other ProActive-specific infos)
     */
    public void beforeOutputMethodInvocation(MethodCall methodCall);

    /**
     * This method is executed when an output invocation has been intercepted, after the execution of the output invocation.
     * @param methodCall the method that has been executed (MethodCall objects include method parameters and other ProActive-specific infos)
     */
    public void afterOutputMethodInvocation(MethodCall methodCall);
}
