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
package unitTests.deployment.descriptorParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.BridgeParsers.AbstractBridgeParser;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentParserImpl;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GroupParsers.AbstractGroupParser;
import org.objectweb.proactive.extra.gcmdeployment.process.bridge.AbstractBridge;
import org.objectweb.proactive.extra.gcmdeployment.process.group.AbstractGroup;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * Add -Djaxp.debug=1 for, well, JAXP debugging
 *
 * @author glaurent
 *
 */
public class TestDeploymentDescriptorParser {
    @Test
    public void test() throws IOException, XPathExpressionException, SAXException, TransformerException,
            ParserConfigurationException {
        File descriptor = new File(this.getClass().getResource("testfiles/deployment.xml").getFile());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(descriptor);

        parser.parseEnvironment();
        parser.parseInfrastructure();
        parser.parseResources();
    }

    @Test
    public void allGroupsTest() throws IOException, XPathExpressionException, SAXException,
            TransformerException, ParserConfigurationException {
        File descriptor = new File(this.getClass().getResource("testfiles/deployment/allGroupsExample.xml")
                .getFile());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(descriptor);

        parser.parseEnvironment();
        parser.parseInfrastructure();
        parser.parseResources();
    }

    //
    // Examples of custom group & bridge parsers
    //
    protected static class UserGroup extends AbstractGroup {
        @Override
        public List<String> internalBuildCommands() {
            return new ArrayList<String>();
        }
    }

    protected static class UserBridge extends AbstractBridge {
        @Override
        public String internalBuildCommand() {
            return "";
        }
    }

    protected static class UserGroupParser extends AbstractGroupParser {
        @Override
        public AbstractGroup createGroup() {
            return new UserGroup();
        }

        @Override
        protected String getNodeNameSpace() {
            return "pauext:";
        }

        @Override
        public String getBaseNodeName() {
            return "myGroup";
        }

        @Override
        public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
            AbstractGroup group = super.parseGroupNode(groupNode, xpath);

            System.out.println("User Group Parser - someattr value = " +
                groupNode.getAttributes().getNamedItem("someattr").getNodeValue());

            return group;
        }
    }

    protected static class UserBridgeParser extends AbstractBridgeParser {
        @Override
        public AbstractBridge createBridge() {
            return new UserBridge();
        }

        @Override
        protected String getNodeNameSpace() {
            return "pauext:";
        }

        @Override
        public String getBaseNodeName() {
            return "myBridge";
        }

        @Override
        public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
            AbstractBridge bridge = super.parseBridgeNode(bridgeNode, xpath);
            System.out.println("User Bridge Parser - someattr value = " +
                bridgeNode.getAttributes().getNamedItem("someattr").getNodeValue());

            return bridge;
        }
    }

    @Test
    public void userSchemaTest() throws IOException, XPathExpressionException, SAXException,
            RuntimeException, TransformerException, ParserConfigurationException {
        File descriptor = new File(getClass().getResource("testfiles/deployment/group_bridge_ext.xml")
                .getFile());

        String userSchema = getClass().getResource("testfiles/deployment/SampleDeploymentExtension.xsd")
                .getFile();

        userSchema = "/home/glaurent/workspace/proactive_trunk/classes/Tests/unitTests/deployment/descriptorParser/testfiles/deployment/SampleDeploymentExtension.xsd";

        ArrayList<String> schemas = new ArrayList<String>();
        schemas.add(userSchema);

        System.out.println("Parsing " + descriptor.getAbsolutePath() + " with custom schema " + userSchema);
        GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(descriptor, schemas);

        parser.registerGroupParser(new UserGroupParser());
        parser.registerBridgeParser(new UserBridgeParser());

        parser.parseEnvironment();
        parser.parseInfrastructure();
        parser.parseResources();
    }

    protected void idConstraintTest(String descriptorLocation) throws XPathExpressionException,
            TransformerException, ParserConfigurationException {
        File descriptor = new File(this.getClass().getResource(descriptorLocation).getFile());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        boolean gotException = false;

        try {
            GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(descriptor);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
            final String errMsg = "Duplicate key value";
            gotException = e.getMessage().contains(errMsg) || e.getException().getMessage().contains(errMsg);
        }

        Assert.assertTrue(gotException);
    }

    @Test
    public void hostIdConstraintTest() throws XPathExpressionException, TransformerException,
            ParserConfigurationException {
        idConstraintTest("testfiles/deployment/duplicateHostId.xml");
    }

    @Test
    public void groupIdConstraintTest() throws XPathExpressionException, TransformerException,
            ParserConfigurationException {
        idConstraintTest("testfiles/deployment/duplicateGroupId.xml");
    }

    @Test
    public void bridgeIdConstraintTest() throws XPathExpressionException, TransformerException,
            ParserConfigurationException {
        idConstraintTest("testfiles/deployment/duplicateBridgeId.xml");
    }

    protected void refConstraintTest(String descriptorLocation) throws XPathExpressionException,
            TransformerException, ParserConfigurationException {
        File descriptor = new File(this.getClass().getResource(descriptorLocation).getFile());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        boolean gotException = false;

        try {
            GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(descriptor);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
            final String errMsg = "not found for identity constraint";
            gotException = e.getMessage().contains(errMsg) || e.getException().getMessage().contains(errMsg);
        }

        Assert.assertTrue(gotException);
    }

    @Test
    public void hostRefIdConstraintTest() throws XPathExpressionException, TransformerException,
            ParserConfigurationException {
        refConstraintTest("testfiles/deployment/missingHostId.xml");
    }

    @Test
    public void groupRefIdConstraintTest() throws XPathExpressionException, TransformerException,
            ParserConfigurationException {
        refConstraintTest("testfiles/deployment/missingGroupId.xml");
    }

    @Test
    public void groupHostRefIdConstraintTest() throws XPathExpressionException, TransformerException,
            ParserConfigurationException {
        refConstraintTest("testfiles/deployment/missingGroupHostId.xml");
    }

    @Test
    public void bridgeRefIdConstraintTest() throws XPathExpressionException, TransformerException,
            ParserConfigurationException {
        refConstraintTest("testfiles/deployment/missingBridgeId.xml");
    }
}
