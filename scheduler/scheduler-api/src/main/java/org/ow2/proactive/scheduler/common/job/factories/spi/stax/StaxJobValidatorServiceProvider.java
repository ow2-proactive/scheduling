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
package org.ow2.proactive.scheduler.common.job.factories.spi.stax;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.ow2.proactive.scheduler.common.exception.JobValidationException;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.Schemas;
import org.ow2.proactive.scheduler.common.job.factories.ValidationUtil;
import org.ow2.proactive.scheduler.common.job.factories.XMLTags;
import org.ow2.proactive.scheduler.common.job.factories.spi.JobValidatorService;


/**
 * Job XML Validator which validates against the XML schema 
 */
public class StaxJobValidatorServiceProvider implements JobValidatorService {

    private XMLInputFactory xmlInputFactory = null;

    public StaxJobValidatorServiceProvider() {
        System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
        xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    }

    @Override
    public void validateJob(File jobFile) throws JobValidationException {
        String findSchemaByNamespaceUsed;
        try {
            findSchemaByNamespaceUsed = findSchemaByNamespaceUsed(jobFile);
            InputStream schemaStream = this.getClass().getResourceAsStream(findSchemaByNamespaceUsed);
            ValidationUtil.validate(jobFile, schemaStream);
        } catch (Exception e) {
            // wrap all occurring exceptions as a schema exception
            throw new JobValidationException(true, e);
        }

    }

    @Override
    public void validateJob(TaskFlowJob job) throws JobValidationException {
        // validate any job
    }

    private String findSchemaByNamespaceUsed(File file)
            throws FileNotFoundException, XMLStreamException, JobValidationException {
        XMLStreamReader cursorRoot = xmlInputFactory.createXMLStreamReader(new FileInputStream(file));
        try {
            while (cursorRoot.hasNext()) {
                String namespace = advanceCursorAndFindSchema(cursorRoot);
                if (namespace != null)
                    return namespace;
            }
            return Schemas.SCHEMA_LATEST.getLocation();
        } catch (Exception e) {
            throw new JobValidationException(e.getMessage(), e);
        } finally {
            if (cursorRoot != null) {
                cursorRoot.close();
            }
        }
    }

    private String advanceCursorAndFindSchema(XMLStreamReader cursorRoot) throws XMLStreamException {
        int eventType;
        String current;
        eventType = cursorRoot.next();
        if (eventType == XMLEvent.START_ELEMENT) {
            current = cursorRoot.getLocalName();
            if (XMLTags.JOB.matches(current)) {
                String namespace = cursorRoot.getName().getNamespaceURI();
                return Schemas.getSchemaByNamespace(namespace).getLocation();
            }
        }
        return null;
    }
}
