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
package org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.group;

import java.util.List;
import java.util.StringTokenizer;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extra.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupGLiteParser extends AbstractGroupSchedulerParser {
    private static final String NODE_NAME = "gLiteGroup";
    private static final String NODE_NAME_RANK = "rank";
    private static final String NODE_NAME_ENVIRONMENT = "environment";
    private static final String NODE_NAME_ARGUMENTS = "arguments";
    private static final String NODE_NAME_STDOUT = "stdout";
    private static final String NODE_NAME_STDERR = "stderr";
    private static final String NODE_NAME_STDIN = "stdin";
    private static final String NODE_NAME_INPUT_SANDBOX = "inputSandbox";
    private static final String NODE_NAME_OUTPUT_SANDBOX = "outputSandbox";
    private static final String NODE_NAME_EXPIRY_TIME = "expiryTime";
    private static final String NODE_NAME_REQUIREMENTS = "requirements";
    private static final String NODE_NAME_DATA_REQUIREMENTS = "dataRequirements";
    private static final String NODE_NAME_CONFIG_FILE = "configFile";
    private static final String NODE_NAME_OUTPUTSE = "outputse";
    
    private static final String ATTR_VIRTUAL_ORGANISATION = "virtualOrganisation";
    private static final String ATTR_MY_PROXY_SERVER = "myProxyServer";
    private static final String ATTR_JOB_TYPE = "jobType";
    private static final String ATTR_NODES_NUMBER = "nodesNumber";
    private static final String ATTR_EXECUTABLE = "executable";
    private static final String ATTR_RETRY_COUNT = "retryCount";
    private static final String ATTR_OUTPUT_FILE = "outputFile";
    private static final String ATTR_PROACTIVE_HOME = "proactive_home";
    private static final String ATTR_JAVA_HOME = "java_home";
    
    private static final String ATTR_INPUT_DATA = "inputData";
    private static final String ATTR_DATA_CATALOG_TYPE = "dataCatalogType";
    private static final String ATTR_DATA_CATALOG = "dataCatalog";
    

    @Override
    public AbstractGroup createGroup() {
        return new GroupGLite();
        
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupGLite gliteGroup = (GroupGLite) super.parseGroupNode(groupNode, xpath);
               
        String t = GCMParserHelper.getAttributeValue(groupNode, ATTR_VIRTUAL_ORGANISATION);
        gliteGroup.setJobVO(t);
        
        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_MY_PROXY_SERVER);
        gliteGroup.setJobMyProxyServer(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOB_TYPE);
        gliteGroup.setJobJobType(t);
        
        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_NODES_NUMBER);
        gliteGroup.setJobNodeNumber(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_EXECUTABLE);
        gliteGroup.setJobExecutable(t);

        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_RETRY_COUNT);
        gliteGroup.setJobRetryCount(t);
        
        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_OUTPUT_FILE);
        gliteGroup.setJobOutputFile(t);
        
        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_PROACTIVE_HOME);
        gliteGroup.setJobProActiveHome(t);
        
        t = GCMParserHelper.getAttributeValue(groupNode, ATTR_JAVA_HOME);
        gliteGroup.setJobJavaHome(t);


        NodeList childNodes = groupNode.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); ++j) {
            Node child = childNodes.item(j);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            
            String nodeName = child.getNodeName();
            String nodeValue = GCMParserHelper.getElementValue(child);
            
            if (nodeName.equals(NODE_NAME_RANK)) {
            	gliteGroup.setRank(nodeValue);
            
            } else if (nodeName.equals(NODE_NAME_ENVIRONMENT)) {
                gliteGroup.setEnvironment(nodeValue);
          
            } else if (nodeName.equals(NODE_NAME_ARGUMENTS)){
                gliteGroup.setArguments(nodeValue);
                
            } else if (nodeName.equals(NODE_NAME_STDOUT)){
            	gliteGroup.setStdout(nodeValue);
            	
            } else if (nodeName.equals(NODE_NAME_STDERR)){
            	gliteGroup.setStderr(nodeValue);
            	
            } else if (nodeName.equals(NODE_NAME_STDIN)){
            	gliteGroup.setStdin(nodeValue);
            	
            } else if (nodeName.equals(NODE_NAME_INPUT_SANDBOX)){
                String sandbox = nodeValue;
                StringTokenizer st = new StringTokenizer(sandbox);
                while (st.hasMoreTokens()) {
                    gliteGroup.addInputSBEntry(st.nextToken());
                }
            	
            } else if (nodeName.equals(NODE_NAME_OUTPUT_SANDBOX)){
                String sandbox = nodeValue;
                StringTokenizer st = new StringTokenizer(sandbox);
                while (st.hasMoreTokens()) {
                    gliteGroup.addOutputSBEntry(st.nextToken());
                }
            	
            } else if (nodeName.equals(NODE_NAME_EXPIRY_TIME)){
            	gliteGroup.setExpiryTime(nodeValue);
            	
            } else if (nodeName.equals(NODE_NAME_REQUIREMENTS)){
            	gliteGroup.setRequirements(nodeValue);
            
            } else if (nodeName.equals(NODE_NAME_CONFIG_FILE)){
            	gliteGroup.setConfigFile(nodeValue);

            } else if (nodeName.equals(NODE_NAME_OUTPUTSE)){
            	gliteGroup.setOutputSE(nodeValue);

            } else if (nodeName.equals(NODE_NAME_DATA_REQUIREMENTS)){
            	gliteGroup.setHasDataRequirements(true);

            	t = GCMParserHelper.getAttributeValue(child, ATTR_INPUT_DATA);
            	if (t==null){
            		return gliteGroup;
            	}
            	gliteGroup.setInputData(t);

            	t = GCMParserHelper.getAttributeValue(child, ATTR_DATA_CATALOG_TYPE);
            	if (t==null){
            		return gliteGroup;
            	}
            	gliteGroup.setDataCatalogType(t);

            	t = GCMParserHelper.getAttributeValue(child, ATTR_DATA_CATALOG);
            	if (t==null){
            		return gliteGroup;
            	}
            	gliteGroup.setDataCatalog(t);

            }
        }

        return gliteGroup;
    }
}
