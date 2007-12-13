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
package doc;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/** Check an xml file follows its dtd.  Greatly inspired from the apache xalan samples 'Validate'  */
public class Validate extends DefaultHandler implements LexicalHandler {
    private String inputFile;
    boolean troubleFound = false;
    boolean hasDTD = false;
    StringBuffer errorBuffer;

    Validate(String sourceFile, StringBuffer errorBuff) {
        super();
        this.inputFile = sourceFile;
        this.errorBuffer = errorBuff;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Syntax:      java Validate fileName");
            System.out
                    .println("This parser tries compares an XML document to its dtd: is it conforming to its DOCTYPE?");

            return;
        }

        System.out.print(parse(args[0]));
    }

    /** Parse an XML file, and validate it.
     * @return a buffer containing the errors (and a comment) */
    static StringBuffer parse(String filename) {
        StringBuffer returnedBuffer = new StringBuffer();

        try {
            File f = new File(filename);
            StringBuffer errorBuff = new StringBuffer();
            InputSource input = new InputSource(new FileInputStream(f));
            // Set systemID so parser can find the dtd with a relative URL in the source document.
            input.setSystemId(f.toString());

            SAXParserFactory spfact = SAXParserFactory.newInstance();

            spfact.setValidating(true);
            spfact.setNamespaceAware(true);

            SAXParser parser = spfact.newSAXParser();

            XMLReader reader = parser.getXMLReader();

            //Instantiate inner-class error and lexical handler.
            Validate handler = new Validate(filename, errorBuff);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            parser.parse(input, handler);

            if (!handler.hasDTD) {
                returnedBuffer.append("[WARNING] NO DOCTYPE DECLARATION in " + filename + "\n");
            } else if (handler.troubleFound) {
                returnedBuffer.append("[ERROR] XML is NOT VALID: it does not comply to dtd in " + filename +
                    "\n");
                returnedBuffer
                        .append("[HINT] If you have trouble spotting exactly where the errors are, set util.DocBookize.SHORT_LINES to true.\n");
                returnedBuffer.append(handler.errorBuffer);
            } else {
                returnedBuffer.append("[INFO] XML is VALID and complies to dtd (" + filename + ")\n");
            }
        } catch (Exception e) // Any problem related to xml structure or file existence
        {
            returnedBuffer.append("[ERROR] File NOT WELL-FORMED :" + filename + ". " + e.getMessage() + "\n");
        }

        return returnedBuffer;
    }

    /** When an error is encountered, store it for future recall */
    @Override
    public void error(SAXParseException exc) {
        String message = exc.getMessage();
        this.errorBuffer.append("[ERROR] " + this.inputFile + "[" + exc.getLineNumber() + "] " + message +
            "\n");

        if (message.startsWith("Element type \"") && message.endsWith("\" must be declared.")) {
            this.errorBuffer.append("[HINT] The dtd does not allow such a tag! ");
        }

        this.troubleFound = true;
    }

    /** When a warning is encountered, store it for future recall */
    @Override
    public void warning(SAXParseException exc) {
        this.errorBuffer.append("[WARNING] " + this.inputFile + "[" + exc.getLineNumber() + "] " +
            exc.getMessage() + "\n");
        this.troubleFound = true;
    }

    /** Set hasDTD to true when dtd is found. */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.hasDTD = true;
    }

    public void endDTD() throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
    }
}
