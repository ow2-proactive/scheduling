/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.job.factories.globalvariables;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.util.Object2ByteConverter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * @author ActiveEon Team
 * @since 13/07/2021
 */
public class GlobalVariablesParser {

    public static final Logger logger = Logger.getLogger(GlobalVariablesParser.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
                                                                                        new NamedThreadFactory("GlobalVariablesParser",
                                                                                                               true));

    private static GlobalVariablesParser instance = null;

    private static String configurationPath = null;

    private List<Filter> loadedFilters = null;

    private String md5 = null;

    private GlobalVariablesParser() {
        if (configurationPath == null) {
            configurationPath = PASchedulerProperties.getAbsolutePath(PASchedulerProperties.GLOBAL_VARIABLES_CONFIGURATION.getValueAsString());
        }
    }

    public static synchronized void setConfigurationPath(String path) {
        configurationPath = path;
    }

    public static synchronized GlobalVariablesParser getInstance() {
        if (instance == null) {
            instance = new GlobalVariablesParser();
            instance.loadFilters();
            int refreshPeriod = PASchedulerProperties.GLOBAL_VARIABLES_REFRESH.getValueAsInt();
            instance.scheduler.scheduleWithFixedDelay(() -> instance.loadFilters(),
                                                      refreshPeriod,
                                                      refreshPeriod,
                                                      TimeUnit.MINUTES);
        }
        return instance;
    }

    public void reloadFilters() {
        instance.loadFilters();
    }

    public synchronized List<Filter> getLoadedFilters() {
        return loadedFilters;
    }

    /**
     * Return the global variables and generic information configured for the given workflow
     * @param jobContent xml workflow as string
     * @return global data containing variables and generic information
     */
    public synchronized GlobalVariablesData getVariablesFor(String jobContent) {

        GlobalVariablesData answer = new GlobalVariablesData();
        Map<String, JobVariable> configuredVariables = new LinkedHashMap<>();
        Map<String, String> configuredGenericInfo = new LinkedHashMap<>();
        answer.setVariables(configuredVariables);
        answer.setGenericInformation(configuredGenericInfo);

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("Error when configuring DocumentBuilder", e);
            return answer;
        }

        try (StringReader reader = new StringReader(jobContent)) {
            InputSource inputSource = new InputSource(reader);
            Document xmlDocument = builder.parse(inputSource);
            XPath xPath = XPathFactory.newInstance().newXPath();
            for (Filter filter : loadedFilters) {
                boolean allMatch = true;
                for (String xpathExpression : filter.getXpath()) {
                    NodeList nodeList = (NodeList) xPath.compile(xpathExpression).evaluate(xmlDocument,
                                                                                           XPathConstants.NODESET);
                    allMatch = allMatch && (nodeList.getLength() > 0);
                }
                if (allMatch) {
                    for (JobVariable variable : filter.getVariables()) {
                        configuredVariables.put(variable.getName(), variable);
                    }

                    for (GenericInformation info : filter.getGenericInformation()) {
                        configuredGenericInfo.put(info.getName(), info.getValue());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error analysing workflow global variables", e);
        }
        return answer;
    }

    private synchronized void loadFilters() {
        loadedFilters = loadFilters(configurationPath);
        try {
            md5 = DigestUtils.md5Hex(Object2ByteConverter.convertObject2Byte(loadedFilters));
        } catch (Exception e) {
            logger.error("Could not compute MD5 of loaded filter", e);
            md5 = "INVALID";
        }
    }

    private List<Filter> loadFilters(String filePath) {

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = docBuilder.parse(filePath);

            org.w3c.dom.Element varElement = document.getDocumentElement();
            JAXBContext context = JAXBContext.newInstance(GlobalVariables.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<GlobalVariables> loader = unmarshaller.unmarshal(varElement, GlobalVariables.class);
            GlobalVariables inputFromXml = loader.getValue();
            return inputFromXml.getFilters();
        } catch (Exception e) {
            logger.error("Error when parsing global variables configuration", e);
            return Collections.emptyList();
        }
    }

    public synchronized String getMD5() {
        return md5;
    }

}
