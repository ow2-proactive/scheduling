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

import testsuite.manager.FunctionalTestManager;

import testsuite.test.FunctionalTest;

import java.util.ArrayList;
import java.util.Hashtable;


/**
 * @author adicosta
 *
 */
public class InterLinkedHandler extends AbstractUnmarshallerDecorator
    implements ManagerDescriptorConstants {
    private FunctionalTestManager manager = null;
    private Hashtable table = null;
    private Group group = null;

    InterLinkedHandler(FunctionalTestManager manager) {
        super();

        this.manager = manager;
        this.manager.setInterLinkedGroups(new ArrayList());

        this.table = new Hashtable();
        addHandler(ID_TEST_TAG, new IdTestHandler(this.table, this.manager));
        addHandler(INTERLINKS_TAG, new InterLinksHandler(this.table));
    }

    public Object getResultObject() throws org.xml.sax.SAXException {
        return manager;
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
        manager.addInterLinkedGroup(group);
    }

    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        if (name.equalsIgnoreCase(ID_TEST_TAG)) {
            group.add((FunctionalTest) activeHandler.getResultObject());
        }
    }

    //-----------------------------------------------------------------------------------------------------------
    private static class IdTestHandler extends AbstractUnmarshallerDecorator {
        private FunctionalTest test = null;
        private FunctionalTestManager manager = null;
        private Hashtable table = null;

        IdTestHandler(Hashtable table, FunctionalTestManager manager) {
            super();
            this.manager = manager;
            this.table = table;
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return this.test;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            Logger logger = manager.getLogger();
            String className = attributes.getValue("class");
            String id = attributes.getValue("id");
            if (checkNonEmpty(className) && checkNonEmpty(id)) {
                try {
                    Class c = this.getClass().getClassLoader().loadClass(className);
                    this.test = (FunctionalTest) c.newInstance();
                    this.test.setManager(this.manager);
                    this.table.put(id, this.test);
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

    //	-----------------------------------------------------------------------------------------------------------
    private static class InterLinksHandler extends AbstractUnmarshallerDecorator {
        private Hashtable table = null;

        InterLinksHandler(Hashtable table) {
            super();
            this.table = table;
            addHandler(LINK_TAG, new LinkHandler(this.table));
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return this.table;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            if (name.equalsIgnoreCase(LINK_TAG)) {
                activeHandler.getResultObject();
            }
        }
    }

    //	  -----------------------------------------------------------------------------------------------------------
    private static class LinkHandler extends AbstractUnmarshallerDecorator {
        private Hashtable table = null;
        private FunctionalTest test = null;
        private ArrayList parents = null;

        LinkHandler(Hashtable table) {
            super();
            this.table = table;
            addHandler(PARENT_TAG, new ParentHandler(this.table));
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            ((FunctionalTest) this.test).setTests((FunctionalTest[]) this.parents.toArray(
                    new FunctionalTest[this.parents.size()]));
            return this.test;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
				this.parents = new ArrayList();
            String id = attributes.getValue("id");
            this.test = (FunctionalTest) this.table.get(id);
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            if (name.equalsIgnoreCase(PARENT_TAG)) {
                this.parents.add(activeHandler.getResultObject());
            }
        }
    }

    //	  -----------------------------------------------------------------------------------------------------------
    private static class ParentHandler extends AbstractUnmarshallerDecorator {
        private Hashtable table = null;
        private FunctionalTest test = null;

        ParentHandler(Hashtable table) {
            super();
            this.table = table;
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return this.test;
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            String id = attributes.getValue("id");
            this.test = (FunctionalTest) this.table.get(id);
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
        }
    }
}
