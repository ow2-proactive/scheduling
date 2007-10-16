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
package org.objectweb.proactive.examples.webservices.c3dWS.ws;

import javax.xml.namespace.QName;


public class Service1Locator extends org.apache.axis.client.Service implements org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1 {
    // Use to get a proxy class for Service1Soap
    private java.lang.String Service1Soap_address = "http://atacama/TestWS/Service1.asmx";

    // The WSDD service name defaults to the port name.
    private java.lang.String Service1SoapWSDDServiceName = "Service1Soap";
    private java.util.HashSet<QName> ports = null;

    public Service1Locator(String url) {
        this.Service1Soap_address = url;
    }

    public java.lang.String getService1SoapAddress() {
        return Service1Soap_address;
    }

    public java.lang.String getService1SoapWSDDServiceName() {
        return Service1SoapWSDDServiceName;
    }

    public void setService1SoapWSDDServiceName(java.lang.String name) {
        Service1SoapWSDDServiceName = name;
    }

    public org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1Soap getService1Soap()
        throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;

        try {
            endpoint = new java.net.URL(Service1Soap_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }

        return getService1Soap(endpoint);
    }

    public org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1Soap getService1Soap(
        java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1SoapStub _stub =
                new org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1SoapStub(portAddress,
                    this);
            _stub.setPortName(getService1SoapWSDDServiceName());

            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setService1SoapEndpointAddress(java.lang.String address) {
        Service1Soap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override
    public java.rmi.Remote getPort(Class serviceEndpointInterface)
        throws javax.xml.rpc.ServiceException {
        try {
            if (org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1Soap.class.isAssignableFrom(
                        serviceEndpointInterface)) {
                org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1SoapStub _stub =
                    new org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1SoapStub(new java.net.URL(
                            Service1Soap_address), this);
                _stub.setPortName(getService1SoapWSDDServiceName());

                return _stub;
            }
        } catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }

        throw new javax.xml.rpc.ServiceException(
            "There is no stub implementation for the interface:  " +
            ((serviceEndpointInterface == null) ? "null"
                                                : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @SuppressWarnings("unchecked")
    @Override
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName,
        Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }

        String inputPortName = portName.getLocalPart();

        if ("Service1Soap".equals(inputPortName)) {
            return getService1Soap();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);

            return _stub;
        }
    }

    @Override
    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "Service1");
    }

    @Override
    public java.util.Iterator<QName> getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet<QName>();
            ports.add(new javax.xml.namespace.QName("Service1Soap"));
        }

        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(java.lang.String portName,
        java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("Service1Soap".equals(portName)) {
            setService1SoapEndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(
                " Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(javax.xml.namespace.QName portName,
        java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }
}
