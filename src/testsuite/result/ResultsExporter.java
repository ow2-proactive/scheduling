/*
 * Created on Aug 1, 2003
 *
 */
package testsuite.result;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;


/**
 * @author Alexandre di Costanzo
 *
 */
public interface ResultsExporter {
    public String toString();

    public void toPrintWriter(PrintWriter out);

    public void toOutPutStream(OutputStream out) throws IOException;

    public Document toXML() throws ParserConfigurationException;

    public void toHTML(File location)
        throws ParserConfigurationException, TransformerException, IOException;
}
