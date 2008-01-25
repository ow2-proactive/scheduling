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
package org.objectweb.proactive.core.xml.handler;

import org.objectweb.proactive.core.xml.io.Attributes;


/**
 *
 * Receives SAX event and pass them on
 *
 * @author       Lionel Mestre
 * @version      0.91
 *
 */
public abstract class AbstractUnmarshallerDecorator implements UnmarshallerHandler {
    private java.util.HashMap<String,UnmarshallerHandler> handlersMap;
    private int elementCounter = 0;
    private UnmarshallerHandler currentActiveHandler;
    private boolean lenient;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //  
    public AbstractUnmarshallerDecorator(boolean lenient) {
        handlersMap = new java.util.HashMap<String,UnmarshallerHandler>();
        this.lenient = lenient;
    }

    public AbstractUnmarshallerDecorator() {
        this(true);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void addHandler(String elementName, UnmarshallerHandler handler) {
        handlersMap.put(elementName, handler);
    }

    //
    // -- implements UnmarshallerHandler ------------------------------------------------------
    //  
    // left abstract 
    //
    // -- implements XMLHandler ------------------------------------------------------
    //  
    public void startElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
        //System.out.println("AbstractCompositeUnmarshaller "+this.getClass().getName()+" startElement="+name);
        elementCounter++;
        if (currentActiveHandler == null) {
            // we look for an handler able to handle the tag
            currentActiveHandler = getHandler(name);
            if (currentActiveHandler == null) {
                if (lenient) {
                    currentActiveHandler = new NullUnmarshallerHandler();
                } else {
                    throw new org.xml.sax.SAXException("Cannot find an handler registered for element " +
                        name);
                }
            }
            currentActiveHandler.startContextElement(name, attributes);
        } else {
            currentActiveHandler.startElement(name, attributes);
        }
    }

    public void endElement(String name) throws org.xml.sax.SAXException {
        //System.out.println("AbstractCompositeUnmarshaller "+this.getClass().getName()+" endElement="+name+"  elementCounter="+elementCounter);
        checkActiveHandler();
        elementCounter--;
        if (elementCounter == 0) {
            // the element that triggered the currentActiveHandler is closed, we set
            // the handler to null
            notifyEndActiveHandler(name, currentActiveHandler);
            currentActiveHandler = null;
        } else {
            currentActiveHandler.endElement(name);
        }
    }

    public void readValue(String value) throws org.xml.sax.SAXException {
        //System.out.println("AbstractCompositeUnmarshaller "+this.getClass().getName()+" readValue="+value);
        if (currentActiveHandler != null) {
            currentActiveHandler.readValue(value);
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
        //  	System.out.println("prefix "+prefix+" uri "+uri);
        checkActiveHandler();
        currentActiveHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
        checkActiveHandler();
        currentActiveHandler.endPrefixMapping(prefix);
    }

    //
    // -- PROTECTED METHODS ------------------------------------------------------
    //  
    protected void checkActiveHandler() throws org.xml.sax.SAXException {
        if (currentActiveHandler == null) {
            throw new org.xml.sax.SAXException("No handler is currently defined");
        }
    }

    protected abstract void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
            throws org.xml.sax.SAXException;

    protected boolean checkNonEmpty(String s) {
        return (s != null) && (s.length() > 0);
    }

    protected UnmarshallerHandler getHandler(String elementName) {
        Object o = handlersMap.get(elementName);
        if (o == null) {
            return null;
        }
        return (UnmarshallerHandler) o;
    }

    //
    // -- PRIVATE METHODS ------------------------------------------------------
    //
    //
    // -- INNER CLASSES ------------------------------------------------------
    //
    private class NullUnmarshallerHandler extends BasicUnmarshaller {
        @Override
        public void startElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            //System.out.println(name+"  ignored");
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            //System.out.println(name+"  ignored");
        }

        @Override
        public Object getResultObject() throws org.xml.sax.SAXException {
            return null;
        }
    }
}
