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
package testsuite.manager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import testsuite.bean.Beanable;

import testsuite.group.Group;

import testsuite.result.ResultsCollections;
import testsuite.result.ResultsExporter;

import testsuite.test.AbstractTest;

import testsuite.xml.ManagerDescriptorHandler;

import testsuite.xslt.TransformerXSLT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


/**
 * @author Alexandre di Costanzo
 *
 */
public abstract class AbstractManager implements ResultsExporter, Beanable {
    private String name = "AbstractManager with no name";
    private String description = "AbstractManager with no description.";
    private ArrayList groups = new ArrayList();
    protected static Logger logger = null;
    private int nbRuns = 1;
    private ResultsCollections results = new ResultsCollections();
    private Properties properties = null;
	protected ArrayList interLinkedGroups = null;

    public AbstractManager() {
        logger = Logger.getLogger(getClass().getName());
        testAppender();
    }

    public AbstractManager(String name, String description) {
        this.name = name;
        this.description = description;
        logger = Logger.getLogger(getClass().getName());
        
        testAppender();
    }

    public AbstractManager(File xmlDescriptor) throws IOException, SAXException {
        logger = Logger.getLogger(getClass().getName());
        testAppender();
        ManagerDescriptorHandler.createManagerDescriptor(xmlDescriptor.getPath(),
            this);
    }

    private void testAppender() {
        int nbAppenders = 0;
        Enumeration enum = Logger.getRootLogger().getAllAppenders();
        while (enum.hasMoreElements())
            nbAppenders++;
        if (nbAppenders == 0) {
            File log = new File(System.getProperty("user.home") +
                    File.separatorChar + "tests.log");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(log);
            } catch (FileNotFoundException e) {
                logger.warn("Log file not found", e);
            }
            Logger.getRootLogger().addAppender(new WriterAppender(
                    new PatternLayout("%d [%t] %-5p %c %x - %m%n"), out));

            Logger.getRootLogger().setLevel((Level) Level.INFO);

        }
        
    }

    public abstract void initManager() throws Exception;

    public abstract void execute(boolean useAttributesFile);

    public abstract void endManager() throws Exception;

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param string
     */
    public void setDescription(String string) {
        description = string;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Group o) {
        return groups.add(o);
    }

    /**
     * @see java.util.Collection#clear()
     */
    public void clear() {
        groups.clear();
    }

    /**
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Group o) {
        return groups.contains(o);
    }

    /**
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        return groups.isEmpty();
    }

    /**
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        return groups.iterator();
    }

    /**
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Group o) {
        return groups.remove(o);
    }

    /**
     * @see java.util.Collection#size()
     */
    public int size() {
        return groups.size();
    }

    /**
     * @see java.util.Collection#toArray()
     */
    public Group[] toArray() {
        return (Group[]) groups.toArray(new Group[groups.size()]);
    }

    /**
     * @return
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return
     */
    public int getNbRuns() {
        return nbRuns;
    }

    /**
     * @param i
     */
    public void setNbRuns(int i) {
        nbRuns = i;
        if (logger.isDebugEnabled()) {
            logger.debug("Nb runs change in " + nbRuns);
        }
    }
    
    public void setNbRuns(String i){
    	this.nbRuns = Integer.parseInt(i);
		if (logger.isDebugEnabled()) {
			logger.debug("Nb runs change in " + this.nbRuns);
		}
    }

    /**
     * @return
     */
    public ArrayList getGroups() {
        return groups;
    }

    /**
     * @param groups
     */
    public void setGroups(ArrayList groups) {
        this.groups = groups;
    }

    /**
     * @return
     */
    public ResultsCollections getResults() {
        return results;
    }

    /**
     * @see testsuite.result.ResultsExporter#toHTML(java.io.File)
     */
    public void toHTML(File location)
        throws ParserConfigurationException, TransformerException, IOException {
        if (logger.isInfoEnabled()) {
            logger.info("Create HMTL file ...");
        }
        String xslPath = "/" +
            AbstractManager.class.getName().replace('.', '/').replaceAll("manager.*",
                "/xslt/manager.xsl");
        TransformerXSLT.transformerTo(toXML(), location, xslPath);

        // copy css
        String cssPath = "/" +
            AbstractManager.class.getName().replace('.', '/').replaceAll("manager.*",
                "/css/stylesheet.css");
        InputStream css = getClass().getResourceAsStream(cssPath);
        File copy = new File(location.getParent() + File.separator +
                "stylesheet.css");
        FileOutputStream out = new FileOutputStream(copy);
        byte[] buffer = new byte[1024];
        int nbBytes = 0;

        while (nbBytes != -1) {
            nbBytes = css.read(buffer);
            if (nbBytes > 0) {
                out.write(buffer, 0, nbBytes);
            }
        }
        css.close();
        out.close();
        if (logger.isInfoEnabled()) {
            logger.info("... Finish creating HTML file");
        }
    }

    /**
     * @see testsuite.result.ResultsExporter#toOutPutStream(java.io.OutputStream)
     */
    public void toOutPutStream(OutputStream out) throws IOException {
        out.write(toString().getBytes());
    }

    /**
     * @see testsuite.result.ResultsExporter#toPrintWriter(java.io.PrintWriter)
     */
    public void toPrintWriter(PrintWriter out) {
        out.println(toString());
    }

    /**
     * @see testsuite.result.ResultsExporter#toXML()
     */
    public Document toXML() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Node root = document.createElement("Manager");

        Node nameNode = document.createElement("Name");
        Node nameNodeText = document.createTextNode(this.name);
        nameNode.appendChild(nameNodeText);
        root.appendChild(nameNode);

        Node descriptionNode = document.createElement("Description");
        Node descriptionNodeText = document.createTextNode(this.description);
        descriptionNode.appendChild(descriptionNodeText);
        root.appendChild(descriptionNode);

        Node date = time(document);
        root.appendChild(date);

        Node runs = document.createElement("NbRuns");
        Node runsText = document.createTextNode(nbRuns + "");
        runs.appendChild(runsText);
        root.appendChild(runs);

        Node groups = document.createElement("Groups");
        Iterator itGroup = iterator();
        while (itGroup.hasNext()) {
            Group group = (Group) itGroup.next();
            Node groupNode = document.createElement("Group");

            Node groupName = document.createElement("Name");
            Node groupNameText = document.createTextNode(group.getName());
            groupName.appendChild(groupNameText);
            groupNode.appendChild(groupName);

            Node groupDescription = document.createElement("Description");
            Node groupDescriptionText = document.createTextNode(group.getDescription());
            groupDescription.appendChild(groupDescriptionText);
            groupNode.appendChild(groupDescription);

            Node groupResults = document.importNode(group.getResults().toXML()
                                                         .getFirstChild(), true);
            groupNode.appendChild(groupResults);

            groups.appendChild(groupNode);
        }
        root.appendChild(groups);

        Node allMessages = document.createElement("AllMessages");
        Node resultsNode = document.importNode(results.toXML().getFirstChild(),
                true);
        allMessages.appendChild(resultsNode);
        root.appendChild(allMessages);

        document.appendChild(root);
        return document;
    }

    private Node time(Document document) {
        Calendar date = Calendar.getInstance();
        Element root = document.createElement("Date");

        root.setAttribute("day", date.get(Calendar.DATE) + "");
        root.setAttribute("month", date.get(Calendar.MONTH) + "");
        root.setAttribute("year", date.get(Calendar.YEAR) + "");

        Element time = document.createElement("Time");
        time.setAttribute("hour", date.get(Calendar.HOUR_OF_DAY) + "");
        time.setAttribute("minute", date.get(Calendar.MINUTE) + "");
        time.setAttribute("second", date.get(Calendar.SECOND) + "");
        time.setAttribute("millisecond", date.get(Calendar.MILLISECOND) + "");
        root.appendChild(time);

        return root;
    }

    /**
     * @see testsuite.result.ResultsExporter#toString()
     */
    public String toString() {
        return results.toString();
    }

    public void setVerbatim(boolean verbatim) {
        results.setVerbatim(verbatim);
    }

    public boolean isVerbatim() {
        return results.isVerbatim();
    }

    /**
     * @see testsuite.bean.Beanable#loadAttributes()
     */
    public void loadAttributes() throws IOException {
        String filename = getClass()
                              .getResource("/" +
                getClass().getName().replace('.', '/') + ".class").getPath();
        filename = filename.replaceAll("\\.class", ".prop");
        loadAttributes(new File(filename.replaceAll("%20", " ")));
    }

    /**
     * @see testsuite.bean.Beanable#loadAttributes(java.io.File)
     */
    public void loadAttributes(File propsFile) throws IOException {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(propsFile);
        props.load(in);
        in.close();

        loadAttributes(props);
    }

    /**
     * @see testsuite.bean.Beanable#loadAttributes(java.util.Properties)
     */
    public void loadAttributes(Properties properties) {
    	if(properties != null){
    	this.properties = properties;
        Class[] parameterTypes = { String.class };
        Enumeration enum = properties.propertyNames();
        Method setter = null;

        while (enum.hasMoreElements()) {
            String name = (String) enum.nextElement();
            String value = properties.getProperty(name);
            try {
                setter = getClass().getMethod("set" + name, parameterTypes);
                Object[] args = { value };
                try {
                    setter.invoke(this, args);
                    if (logger.isDebugEnabled()) {
                        logger.debug("set " + name + " with " + value);
                    }
                } catch (IllegalArgumentException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e1);
                    }
                } catch (IllegalAccessException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e1);
                    }
                } catch (InvocationTargetException e1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e1);
                    }
                }
            } catch (SecurityException e) {
                // do nothing
            } catch (NoSuchMethodException e) {
                // do nothing
            }
        }

        for (int i = 0; i < groups.size(); i++) {
            Group group = (Group) groups.get(0);
            Iterator it = group.iterator();
            while (it.hasNext()) {
                AbstractTest test = (AbstractTest) it.next();
                test.loadAttributes(properties);
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("Test Suite's attributes readed");
        }
    	}
    }

    /**
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * @param properties
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
