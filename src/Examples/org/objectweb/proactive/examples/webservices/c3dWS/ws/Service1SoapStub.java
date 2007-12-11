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


public class Service1SoapStub extends org.apache.axis.client.Stub implements org.objectweb.proactive.examples.webservices.c3dWS.ws.Service1Soap {
    static org.apache.axis.description.OperationDesc[] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[7];
        _initOperationDesc1();
    }

    private java.util.Vector<Class<?>> cachedSerClasses = new java.util.Vector<Class<?>>();
    private java.util.Vector<QName> cachedSerQNames = new java.util.Vector<QName>();
    private java.util.Vector<Class<?>> cachedSerFactories = new java.util.Vector<Class<?>>();
    private java.util.Vector<Class<?>> cachedDeserFactories = new java.util.Vector<Class<?>>();

    public Service1SoapStub() throws org.apache.axis.AxisFault {
        this(null);
    }

    public Service1SoapStub(java.net.URL endpointURL,
        javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        this(service);
        super.cachedEndpoint = endpointURL;
    }

    public Service1SoapStub(javax.xml.rpc.Service service)
        throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }

        java.lang.Class<?> cls;
        javax.xml.namespace.QName qName;
        java.lang.Class<?> beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class<?> beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class<?> enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class<?> enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        java.lang.Class<?> arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        java.lang.Class<?> arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        java.lang.Class<?> simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        java.lang.Class<?> simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        qName = new javax.xml.namespace.QName("http://tempuri.org/",
                "ArrayOfInt");
        cachedSerQNames.add(qName);
        cls = org.objectweb.proactive.examples.webservices.c3dWS.ws.ArrayOfInt.class;
        cachedSerClasses.add(cls);
        cachedSerFactories.add(beansf);
        cachedDeserFactories.add(beandf);
    }

    private static void _initOperationDesc1() {
        org.apache.axis.description.OperationDesc oper;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("HelloWorld");
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "name"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        //        oper.setStyle(Style.WRAPPED);
        //        oper.setUse(Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("setPixels");
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "userName"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "pix"),
            new javax.xml.namespace.QName("http://tempuri.org/", "ArrayOfInt"),
            org.objectweb.proactive.examples.webservices.c3dWS.ws.ArrayOfInt.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "number"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "nbInt"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        //        oper.setStyle(Style.WRAPPED);
        //        oper.setUse(Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ShowDialog");
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "userName"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "message"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        //        oper.setStyle(Style.WRAPPED);
        //        oper.setUse(Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("showUserMessage");
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "userName"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "message"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        //        oper.setStyle(Style.WRAPPED);
        //        oper.setUse(Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("showUserMessageFrom");
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "userName"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "from"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "message"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        //        oper.setStyle(Style.WRAPPED);
        //        oper.setUse(Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("informNewUser");
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "myUserName"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "usernameNew"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "id"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "int"), int.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        //        oper.setStyle(Style.WRAPPED);
        //        oper.setUse(Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("informUserLeft");
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "myUserName"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.addParameter(new javax.xml.namespace.QName("http://tempuri.org/",
                "userNameLeft"),
            new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema",
                "string"), java.lang.String.class,
            org.apache.axis.description.ParameterDesc.IN, false, false);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        //        oper.setStyle(Style.WRAPPED);
        //        oper.setUse(Use.LITERAL);
        _operations[6] = oper;
    }

    private org.apache.axis.client.Call createCall()
        throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = (org.apache.axis.client.Call) super.service.createCall();

            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }

            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }

            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }

            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }

            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }

            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }

            java.util.Enumeration keys = super.cachedProperties.keys();

            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }

            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);

                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class<?> cls = cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName = cachedSerQNames.get(i);
                        java.lang.Class<?> sf = cachedSerFactories.get(i);
                        java.lang.Class<?> df = cachedDeserFactories.get(i);
                        _call.registerTypeMapping(cls, qName, sf, df, false);
                    }
                }
            }

            return _call;
        } catch (java.lang.Throwable t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object",
                t);
        }
    }

    public void helloWorld(java.lang.String helloWorldName)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/HelloWorld");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "HelloWorld"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    helloWorldName
                });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        }

        extractAttachments(_call);
    }

    public void setPixels(java.lang.String setPixelsUserName,
        org.objectweb.proactive.examples.webservices.c3dWS.ws.ArrayOfInt setPixelsPix,
        int setPixelsNumber, int setPixelsNbInt)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/setPixels");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "setPixels"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    setPixelsUserName, setPixelsPix,
                    new java.lang.Integer(setPixelsNumber),
                    new java.lang.Integer(setPixelsNbInt)
                });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        }

        extractAttachments(_call);
    }

    public void showDialog(java.lang.String showDialogUserName,
        java.lang.String showDialogMessage) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/ShowDialog");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "ShowDialog"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    showDialogUserName, showDialogMessage
                });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        }

        extractAttachments(_call);
    }

    public void showUserMessage(java.lang.String showUserMessageUserName,
        java.lang.String showUserMessageMessage)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/showUserMessage");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "showUserMessage"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    showUserMessageUserName, showUserMessageMessage
                });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        }

        extractAttachments(_call);
    }

    public void showUserMessageFrom(
        java.lang.String showUserMessageFromUserName,
        java.lang.String showUserMessageFromFrom,
        java.lang.String showUserMessageFromMessage)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/showUserMessageFrom");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "showUserMessageFrom"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    showUserMessageFromUserName, showUserMessageFromFrom,
                    showUserMessageFromMessage
                });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        }

        extractAttachments(_call);
    }

    public void informNewUser(java.lang.String informNewUserMyUserName,
        java.lang.String informNewUserUsernameNew, int informNewUserId)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/informNewUser");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "informNewUser"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    informNewUserMyUserName, informNewUserUsernameNew,
                    new java.lang.Integer(informNewUserId)
                });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        }

        extractAttachments(_call);
    }

    public void informUserLeft(java.lang.String informUserLeftMyUserName,
        java.lang.String informUserLeftUserNameLeft)
        throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }

        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://tempuri.org/informUserLeft");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
            Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
            Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName(
                "http://tempuri.org/", "informUserLeft"));

        setRequestHeaders(_call);
        setAttachments(_call);

        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {
                    informUserLeftMyUserName, informUserLeftUserNameLeft
                });

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException) _resp;
        }

        extractAttachments(_call);
    }
}
