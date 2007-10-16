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
public class CollectionUnmarshaller extends AbstractUnmarshallerDecorator {
    protected java.util.ArrayList resultList;
    protected Class<?> targetClass;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //  
    public CollectionUnmarshaller(boolean lenient) {
        this(null, lenient);
    }

    public CollectionUnmarshaller() {
        this(null);
    }

    public CollectionUnmarshaller(Class<?> targetClass, boolean lenient) {
        super(lenient);
        this.targetClass = targetClass;
    }

    public CollectionUnmarshaller(Class<?> targetClass) {
        super();
        this.targetClass = targetClass;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements UnmarshallerHandler ------------------------------------------------------
    //  
    public Object getResultObject() throws org.xml.sax.SAXException {
        int size = 0;
        if (resultList != null) {
            size = resultList.size();
        }
        Object[] resultArray = null;
        if (targetClass == null) {
            resultArray = new Object[size];
        } else {
            resultArray = (Object[]) java.lang.reflect.Array.newInstance(targetClass,
                    size);
        }
        if (size > 0) {
            resultList.toArray(resultArray);
        }

        // clean-up
        resultList = null;
        //targetClass = null;
        return resultArray;
    }

    public void startContextElement(String name, Attributes attributes)
        throws org.xml.sax.SAXException {
        resultList = new java.util.ArrayList();
    }

    //
    // -- PROTECTED METHODS ------------------------------------------------------
    //
    @Override
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        Object o = activeHandler.getResultObject();
        if (o != null) {
            resultList.add(o);
        }
    }

    //
    // -- PRIVATE METHODS ------------------------------------------------------
    //
    //
    // -- INNER CLASSES ------------------------------------------------------
    //
}
