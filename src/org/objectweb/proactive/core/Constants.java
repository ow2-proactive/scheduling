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
package org.objectweb.proactive.core;


/**
 * Defines many constants useful across ProActive
 *
 * @author  ProActive Team
 * @version 1.0,  2002/03/21
 * @since   ProActive 0.9
 *
 */
public interface Constants {

    /** The explicit local body default class */
    public static final Class DEFAULT_BODY_CLASS = org.objectweb.proactive.core.body.ActiveBody.class;

    /** The name of the explicit local body default class */
    public static final String DEFAULT_BODY_CLASS_NAME = DEFAULT_BODY_CLASS.getName();

    /** The explicit local body default class */
    public static final Class DEFAULT_BODY_INTERFACE = org.objectweb.proactive.Body.class;

    /** The name of the explicit local body default class */
    public static final String DEFAULT_BODY_INTERFACE_NAME = DEFAULT_BODY_INTERFACE.getName();

    /** The explicit local body default class */
    public static final Class DEFAULT_BODY_PROXY_CLASS = org.objectweb.proactive.core.body.proxy.UniversalBodyProxy.class;

    /** The name of the explicit local body default class */
    public static final String DEFAULT_BODY_PROXY_CLASS_NAME = DEFAULT_BODY_PROXY_CLASS.getName();

    /** The explicit local body default class */
    public static final Class DEFAULT_FUTURE_PROXY_CLASS = org.objectweb.proactive.core.body.future.FutureProxy.class;

    /** The name of the explicit local body default class */
    public static final String DEFAULT_FUTURE_PROXY_CLASS_NAME = DEFAULT_FUTURE_PROXY_CLASS.getName();

    /**
     * The interface implemented by all proxies featuring 'future' semantics,
     * depending on whether they are remoteBodyly-accessible or not
     */
    public static final Class FUTURE_PROXY_INTERFACE = org.objectweb.proactive.core.body.future.Future.class;

    /** rmi protocol identifier */
    public static final String RMI_PROTOCOL_IDENTIFIER = "rmi:";

    /** rmi tunneling over ssh protocol identifier */
    public static final String RMISSH_PROTOCOL_IDENTIFIER = "rmissh:";

    /** jini protocol identifier */
    public static final String JINI_PROTOCOL_IDENTIFIER = "jini:";
    public static final String IBIS_PROTOCOL_IDENTIFIER = "ibis:";

    /**xml-http protocol identifier */
    public static final String XMLHTTP_PROTOCOL_IDENTIFIER = "http:";

    /** default protocol identifier */
    public static final String DEFAULT_PROTOCOL_IDENTIFIER = RMI_PROTOCOL_IDENTIFIER;
}
