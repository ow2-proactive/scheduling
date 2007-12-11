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
package org.objectweb.proactive.extra.gcmdeployment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class GCMDescriptorProcessor {
    Map<String, String> vmap;
    protected Document document;

    public GCMDescriptorProcessor(Map<String, String> vmap, Document document) {
        this.vmap = vmap;
        this.document = document;
    }

    public void transform(OutputStream output)
        throws XPathExpressionException, SAXException, TransformerException {
        String[] nameList = vmap.keySet().toArray(new String[0]);
        String[] valueList = new String[nameList.length];
        for (int i = 0; i < nameList.length; i++) {
            valueList[i] = vmap.get(nameList[i]);
        }

        System.setProperty("javax.xml.transform.TransformerFactory",
            "net.sf.saxon.TransformerFactoryImpl");
        DOMSource domSource = new DOMSource(document);
        TransformerFactory tfactory = TransformerFactory.newInstance();

        Source stylesheetSource = new StreamSource(this.getClass()
                                                       .getResourceAsStream("variables.xsl"));

        Transformer transformer = null;
        try {
            transformer = tfactory.newTransformer(stylesheetSource);
            transformer.setParameter("nameList", nameList);
            transformer.setParameter("valueList", valueList);
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
            throw e;
        }
    }
}
