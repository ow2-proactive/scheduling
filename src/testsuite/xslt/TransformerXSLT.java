/*
 * Created on Aug 27, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
 * @author adicosta
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
