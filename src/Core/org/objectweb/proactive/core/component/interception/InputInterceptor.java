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
 * incoming functional invocations. <br>
 * Before executing (in the case of a primitive component) or transferring (in
 * the case of a composite component) a functional request, the
 * <code> beforeInputMethodInvocation  </code> method is called, and the
 * <code> afterInputMethodInvocation  </code> is called after the execution or
 * transfer of the invocation. <br>
 * These methods are executed on the controllers of the current component that
 * implement this interface. <br>
 * The <code> beforeInputMethodInvocation  </code> method is called sequentially
 * for each controller in the order they are defined in the controllers
 * configuration file. <br>
 * The <code> afterInputMethodInvocation  </code> method is called sequentially
 * for each controller in the <b> reverse order </b> they are defined in the
 * controllers configuration file. <br>
 * Example : <br>
 * if in the controller config file, the list of input interceptors is in this
 * order (the order in the controller config file is from top to bottom) : <br>
 * <code> InputInterceptor1  </code> <br>
 * <code> InputInterceptor2  </code> <br>
 * This means that an invocation on a server interface will follow this path :
 * <br>
 * <code> --> caller  </code> <br>
 * <code> --> InputInterceptor1.beforeInputMethodInvocation  </code> <br>
 * <code> --> InputInterceptor2.beforeInputMethodInvocation  </code> <br>
 * <code> --> callee.invocation  </code> <br>
 * <code> --> InputInterceptor2.afterInputMethodInvocation  </code> <br>
 * <code> --> InputInterceptor1.afterInputMethodInvocation  </code>
 *
 * @author Matthieu Morel
 */
@PublicAPI
public interface InputInterceptor {

    /**
     * This method is executed when an input invocation is intercepted, before executing the input invocation.
     * @param methodCall the method to be executed (MethodCall objects include method parameters and other ProActive-specific infos)
     */
    public void beforeInputMethodInvocation(MethodCall methodCall);

    /**
     * This method is executed when an input invocation has been intercepted, after the execution of the input invocation.
     * @param methodCall the method that has been executed (MethodCall objects include method parameters and other ProActive-specific infos)
     */
    public void afterInputMethodInvocation(MethodCall methodCall);
}
