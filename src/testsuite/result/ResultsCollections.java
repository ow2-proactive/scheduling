/*
 * Created on Jul 23, 2003
 *
 */
package testsuite.result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import testsuite.exception.BadTypeException;


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
        TransformerFactory tFactory = TransformerFactory.newInstance();
        String xslPath = "/" +
            ResultsCollections.class.getName().replace('.', '/').replaceAll("result.*",
                "/xslt/results.xsl");
        InputStream stylesheet = getClass().getResourceAsStream(xslPath);
        FileOutputStream os = new FileOutputStream(location);
        Transformer transformer = tFactory.newTransformer(new StreamSource(
                    stylesheet));
        DOMSource xml = new DOMSource(toXML());

        transformer.transform(xml, new StreamResult(os));
        os.close();
        stylesheet.close();
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
