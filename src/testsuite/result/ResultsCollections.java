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
package testsuite.result;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import testsuite.exception.BadTypeException;

import testsuite.xslt.TransformerXSLT;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


/**
 * @author Alexandre di Costanzo
 *
 */
public class ResultsCollections implements ResultsExporter {
    private ArrayList results = null;
    private boolean verbatim = false;

    public ResultsCollections() {
        results = new ArrayList();
    }

    public ResultsCollections(ResultsCollections resultsCollection) {
        this.results = new ArrayList(resultsCollection.results);
    }

    public boolean add(int type, String message) throws BadTypeException {
        return add(new TestResult(null, type, message));
    }

    public boolean add(int type, String message, Throwable e)
        throws BadTypeException {
        return add(new TestResult(null, type, message, e));
    }

    public boolean add(AbstractResult result) {
        return results.add(result);
    }

    public void addAll(ResultsCollections resultsCollection) {
        results.addAll(resultsCollection.results);
    }

    public void clear() {
        results.clear();
    }

    public boolean contains(AbstractResult result) {
        return results.contains(result);
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    public Iterator iterator() {
        return results.iterator();
    }

    public boolean remove(TestResult result) {
        return results.remove(result);
    }

    public int size() {
        return results.size();
    }

    public AbstractResult[] toArray() {
        return (AbstractResult[]) results.toArray(new AbstractResult[results.size()]);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String res = "";
        Iterator it = iterator();
        if (verbatim) {
            while (it.hasNext())
                res += ((AbstractResult) it.next()).toString();
        } else {
            while (it.hasNext()) {
                AbstractResult result = (AbstractResult) it.next();
                if (result.getType() > -1) {
                    res += result.toString();
                }
            }
        }
        return res;
    }

    public void toPrintWriter(PrintWriter out) {
        out.println(toString());
    }

    public void toOutPutStream(OutputStream out) throws IOException {
        out.write(toString().getBytes());
    }

    public void toHTML(File location)
        throws ParserConfigurationException, TransformerException, IOException {
        String xslPath = "/" +
            ResultsCollections.class.getName().replace('.', '/').replaceAll("result.*",
                "/xslt/results.xsl");
        TransformerXSLT.transformerTo(toXML(), location, xslPath);
    }

    public Document toXML() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Node root = document.createElement("Results");
        Iterator it = iterator();
        while (it.hasNext()) {
            AbstractResult result = (AbstractResult) it.next();
            root.appendChild(result.toXMLNode(document));
        }
        document.appendChild(root);
        return document;
    }

    public boolean isVerbatim() {
        return verbatim;
    }

    public void setVerbatim(boolean verbatim) {
        this.verbatim = verbatim;
    }
}
