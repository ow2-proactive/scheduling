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
package testsuite.xslt;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * @author Alexandre di Costanzo
 *
 */
public class TransformerXSLT {
    private static TransformerFactory tFactory = TransformerFactory.newInstance();

    public static void transformerTo(Document xml, File destination,
        String xslPath)
        throws ParserConfigurationException, TransformerException, IOException {
        InputStream stylesheet = TransformerXSLT.class.getResourceAsStream(xslPath);
        FileOutputStream os = new FileOutputStream(destination);
        Transformer transformer = tFactory.newTransformer(new StreamSource(
                    stylesheet));
        DOMSource xmlDOM = new DOMSource(xml);

        transformer.transform(xmlDOM, new StreamResult(os));
        os.close();
        stylesheet.close();
    }
}
