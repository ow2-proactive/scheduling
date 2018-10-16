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
package org.ow2.proactive.scheduler.common.job.factories;

import static org.ow2.proactive.scheduler.common.job.factories.XMLTags.SCRIPT_EXECUTABLE;
import static org.ow2.proactive.scheduler.common.job.factories.XMLTags.TASK;
import static org.ow2.proactive.scheduler.common.job.factories.XMLTags.TASK_FLOW;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import org.apache.commons.io.IOUtils;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierHandler;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import com.sun.msv.verifier.ErrorInfo;
import com.sun.msv.verifier.ValidityViolation;


/**
 * Utility class for validate job descriptor (job.xml) files.
 */
public class ValidationUtil {

    /**
     * Validates the job descriptor file against the specified schema.
     * 
     * @param jobInputStream
     *            the job file content as an InputStream
     * @param schemaIs
     *            the job schema
     * 
     * @throws JobCreationException
     *             if the job descriptor is invalid
     */
    public static void validate(InputStream jobInputStream, InputStream schemaIs)
            throws SAXException, IOException, JobCreationException {
        try {

            XMLReader reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            VerifierFactory vfactory = new com.sun.msv.verifier.jarv.TheFactoryImpl();
            Schema schema = vfactory.compileSchema(schemaIs);

            Verifier verifier = schema.newVerifier();
            VerifierHandler handler = verifier.getVerifierHandler();
            ContentHandlerDecorator contentHandlerDecorator = new ContentHandlerDecorator(handler);
            reader.setContentHandler(contentHandlerDecorator);
            ValidationErrorHandler errHandler = new ValidationErrorHandler(contentHandlerDecorator);
            verifier.setErrorHandler(errHandler);

            reader.parse(new InputSource(jobInputStream));
        } catch (SAXException se) {
            Throwable cause = se.getCause();
            if (cause != null && cause instanceof JobCreationException) {
                // unwrap
                throw (JobCreationException) cause;
            } else {
                throw se;
            }
        } catch (VerifierConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static byte[] getInputStreamBytes(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            IOUtils.copy(inputStream, outputStream);
            outputStream.toByteArray();
            return outputStream.toByteArray();
        }
    }

    private static class ValidationErrorHandler implements ErrorHandler {
        private ContentHandlerDecorator decorator;

        ValidationErrorHandler(ContentHandlerDecorator decorator) {
            this.decorator = decorator;
        }

        @Override
        public void error(SAXParseException se) throws SAXException {
            handleError(se);
        }

        @Override
        public void fatalError(SAXParseException se) throws SAXException {
            handleError(se);

        }

        public void warning(SAXParseException se) throws SAXException {
            handleError(se);
        }

        private void handleError(SAXParseException se) throws SAXException {
            StringBuilder sb = new StringBuilder();
            if (se instanceof ValidityViolation) {
                ErrorInfo errorInfo = ((ValidityViolation) se).getErrorInfo();
                if (errorInfo instanceof ErrorInfo.IncompleteContentModel) {
                    String qName = ((ErrorInfo.IncompleteContentModel) errorInfo).qName;
                    if (TASK_FLOW.getXMLName().equals(qName)) {
                        sb.append("Uncompleted job: At least one task must be defined.").append('\n');
                    } else if (TASK.getXMLName().equals(qName)) {
                        sb.append("Uncompleted task: At least the Executable element must be define.").append('\n');
                    } else if (SCRIPT_EXECUTABLE.getXMLName().equals(qName)) {
                        sb.append("Uncompleted scriptExecutable.").append('\n');
                    }
                }
            }
            sb.append("Error: ")
              .append(se.getMessage())
              .append(" at line ")
              .append(se.getLineNumber())
              .append(" , column ")
              .append(se.getColumnNumber())
              .append('.');
            JobCreationException ne = new JobCreationException(sb.toString(), se);
            String currentTask = decorator.currentTask();
            if (currentTask != null && !currentTask.isEmpty()) {
                ne.setTaskName(currentTask);
            }
            throw new SAXException(ne);
        }
    }

    private static class ContentHandlerDecorator implements ContentHandler {
        private ContentHandler handler;

        private Stack<String> currentTask = new Stack<String>();

        private Stack<String> currentE = new Stack<String>();

        ContentHandlerDecorator(ContentHandler handler) {
            this.handler = handler;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            handler.setDocumentLocator(locator);
        }

        @Override
        public void startDocument() throws SAXException {
            handler.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            handler.endDocument();
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            handler.startPrefixMapping(prefix, uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            handler.endPrefixMapping(prefix);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (TASK.getXMLName().equals(currentE.push(qName))) {
                nameAttrValue(atts);
                currentTask.push(nameAttrValue(atts));
            }
            handler.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (TASK.getXMLName().equals(currentE.pop())) {
                currentTask.pop();
            }
            handler.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            handler.characters(ch, start, length);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            handler.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            handler.processingInstruction(target, data);
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            handler.skippedEntity(name);
        }

        public String currentTask() {
            return (currentTask.isEmpty()) ? null : currentTask.peek();
        }

        private String nameAttrValue(Attributes atts) {
            return (atts.getValue("name") == null) ? "" : atts.getValue("name");
        }
    }

}
