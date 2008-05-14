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
package org.objectweb.proactive.core.remoteobject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.ReifiedCastException;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.URIBuilder;


public class RemoteObjectHelper {

    /**
     * returns the default port set for the given protocol or -1 if none
     * @param protocol
     * @return the default port number associated to the protocol or -1 if none
     * @throws UnknownProtocolException
     */
    public static int getDefaultPortForProtocol(String protocol) throws UnknownProtocolException {
        if (Constants.XMLHTTP_PROTOCOL_IDENTIFIER.equals(protocol)) {
            // http port could change according the availability of the default port when activated
            // so we first instantiante the factory which will set the new port if necessary
            getRemoteObjectFactory(protocol);

            if (PAProperties.PA_XMLHTTP_PORT.getValue() != null) {
                return Integer.parseInt(PAProperties.PA_XMLHTTP_PORT.getValue());
            }
        } else if ((Constants.RMI_PROTOCOL_IDENTIFIER.equals(protocol)) ||
            Constants.IBIS_PROTOCOL_IDENTIFIER.equals(protocol) ||
            Constants.RMISSH_PROTOCOL_IDENTIFIER.equals(protocol)) {
            return Integer.parseInt(PAProperties.PA_RMI_PORT.getValue());
        }

        // default would be to return the RMI default port
        return -1;
    }

    /**
     * returns an url for a object to be exposed on the current host for a given
     * protocol and name
     *
     * @param protocol
     * @return the default port number associated to the protocol
     * @throws UnknownProtocolException
     */
    public static URI generateUrl(String protocol, String name) throws UnknownProtocolException {
        return URIBuilder.buildURI(null, name, protocol, getDefaultPortForProtocol(protocol), true);
    }

    /**
     * returns an url for a object to be exposed on the current host for the
     * default protocol and name
     *
     * @param name
     * @return the URI for the given name
     */
    public static URI generateUrl(String name) {
        String protocol = PAProperties.PA_COMMUNICATION_PROTOCOL.getValue();
        try {
            return URIBuilder.buildURI(null, name, protocol, getDefaultPortForProtocol(protocol), true);
        } catch (UnknownProtocolException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param protocol
     * @return return the remote object factory for a given protocol
     * @throws UnknownProtocolException
     */
    public static RemoteObjectFactory getRemoteObjectFactory(String protocol) throws UnknownProtocolException {
        return AbstractRemoteObjectFactory.getRemoteObjectFactory(protocol);
    }

    /**
     * @param url
     * @return eturn the remote object factory for the protocol contained within the url
     * @throws UnknownProtocolException
     */
    public static RemoteObjectFactory getFactoryFromURL(URI url) throws UnknownProtocolException {
        url = expandURI(url);
        return getRemoteObjectFactory(url.getScheme());
    }

    /**
     * @param url url of the registry
     * @return return the list of objects (not only remote objects) registered in the registry identified by the param url
     * @throws ProActiveException
     */
    public static URI[] list(URI url) throws ProActiveException {
        return getFactoryFromURL(url).list(expandURI(url));
    }

    /**
     * make the url 'absolute' by explicitly setting all the possibly not set default values
     * @param uri
     * @return the uri with all values set
     */
    public static URI expandURI(URI uri) {
        if (uri.getScheme() == null) {
            int port = uri.getPort();
            if (port == -1) {
                uri = URIBuilder.buildURIFromProperties(uri.getHost(), uri.getPath());
            } else {
                uri = URIBuilder.setProtocol(uri, PAProperties.PA_COMMUNICATION_PROTOCOL.getValue());
            }
        }
        return uri;
    }

    /**
     * register a remote object at the endpoint identified by the url
     * @param target the remote object to register
     * @param url the url where to register the remote object
     * @param replacePreviousBinding true if any previous bindng as to be replaced
     * @return return a remote reference on the remote object (aka a RemoteRemoteObject)
     * @throws ProActiveException
     */
    public static RemoteRemoteObject register(RemoteObject target, URI url, boolean replacePreviousBinding)
            throws ProActiveException {
        return getFactoryFromURL(url).register(new InternalRemoteRemoteObjectImpl(target), expandURI(url),
                replacePreviousBinding);
    }

    /**
     * unregister the object located at the endpoint identified by the url
     * @param url
     * @throws ProActiveException
     */
    public static void unregister(URI url) throws ProActiveException {
        getFactoryFromURL(url).unregister(expandURI(url));
    }

    /**
     * perform a lookup on the url in order to retrieve a reference on the remote object identified by this url
     * @param url
     * @return a remote object adapter wrapping a remote reference to a remote object
     * @throws ProActiveException
     */
    public static RemoteObject lookup(URI url) throws ProActiveException {
        return getFactoryFromURL(url).lookup(expandURI(url));
    }

    /**
     * generate a couple stub + proxy on the given remote object and set the remote object as target of the proxy
     * @param ro the remote object
     * @return the couple stub + proxy on the given remote object
     * @throws ProActiveException if the stub generation has failed or if the remote object is no longer available
     */
    @SuppressWarnings("unchecked")
    public static <T> T generatedObjectStub(RemoteObject<T> ro) throws ProActiveException {
        try {
            T reifiedObjectStub = (T) MOP.createStubObject(ro.getClassName(), ro.getTargetClass(),
                    new Class[] {});

            ((StubObject) reifiedObjectStub).setProxy(new SynchronousProxy(null, new Object[] { ro }));

            Class<Adapter<T>> adapter = (Class<Adapter<T>>) ro.getAdapterClass();

            if (adapter != null) {
                Class<?>[] classArray = new Class<?>[] {};

                Constructor<?>[] c = adapter.getConstructors();

                Adapter<T> ad = adapter.getConstructor(classArray).newInstance();
                ad.setTargetAndCallConstruct(reifiedObjectStub);
                return (T) ad;
            } else {
                return reifiedObjectStub;
            }
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ReifiedCastException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
