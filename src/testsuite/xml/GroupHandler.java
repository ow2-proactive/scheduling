/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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
package testsuite.xml;

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import testsuite.group.Group;

import testsuite.manager.AbstractManager;

import testsuite.test.AbstractTest;


/**
 * @author adicosta
 *
 */
public class GroupHandler {
    //-----------------------------------------------------------------------------------------------------------
    public static class SimpleGroupHandler extends AbstractUnmarshallerDecorator
        implements ManagerDescriptorConstants {
        private Group group = null;
        private AbstractManager manager = null;

        SimpleGroupHandler(AbstractManager manager) {
            super();
            this.manager = manager;
            addHandler(UNIT_TEST_TAG, new UnitTestHandler(manager));
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return group;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            group = new Group();
            String groupName = attributes.getValue("name");
            if (checkNonEmpty(groupName)) {
                group.setName(groupName);
            }
            String description = attributes.getValue("description");
            if (checkNonEmpty(description)) {
                group.setDescription(description);
            }
            manager.add(group);
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            if (name.equalsIgnoreCase(UNIT_TEST_TAG)) {
                group.add((AbstractTest) activeHandler.getResultObject());
            }
        }
    }

    //-----------------------------------------------------------------------------------------------------------
    private static class UnitTestHandler extends AbstractUnmarshallerDecorator {
        private AbstractTest test = null;
        private AbstractManager manager = null;

        UnitTestHandler(AbstractManager manager) {
            super();
            this.manager = manager;
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return test;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            Logger logger = manager.getLogger();
            String className = attributes.getValue("class");
            if (checkNonEmpty(className)) {
                try {
                    Class c = this.getClass().getClassLoader().loadClass(className);
                    test = (AbstractTest) c.newInstance();
                } catch (ClassNotFoundException e) {
                    logger.fatal("Can't found " + className, e);
                } catch (InstantiationException e) {
                    logger.fatal("Can't create a new instance of " + className,
                        e);
                } catch (IllegalAccessException e) {
                    logger.fatal("Can't access " + className, e);
                }
            }
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }
    }
}
