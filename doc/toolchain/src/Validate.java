/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package doc;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Check an xml file follows its dtd. Greatly inspired from the apache xalan
 * samples 'Validate'
 */
public class Validate extends DefaultHandler implements LexicalHandler {
    private final String inputFile;
    /** Flag set to false when the file is not well formed */
    private boolean valid = true;
    private boolean hasDTD = false;
    StringBuffer errorBuffer;

    Validate(final String sourceFile, final StringBuffer errorBuff) {
        super();
        this.inputFile = sourceFile;
        this.errorBuffer = errorBuff;
        this.parse();
    }

    public static void main(final String[] args) {
        if (args.length != 1) {
            System.out.println("Syntax:      java Validate fileName");
            System.out.println("This parser tries compares an XML "
                + "document to its dtd: is it conforming to its DOCTYPE?");
            return;
        }
        final StringBuffer errorBuffer = new StringBuffer();

        final Validate validator = new Validate(args[0], errorBuffer);

        System.out.print(validator.parse());
        if (!validator.isValid()) {
            throw new Error("The XML is not valid or files are missing.");
        }

    }

    /**
     * Parse an XML file, and validate it.
     * 
     * @return a buffer containing the errors (and a comment)
     */
    private StringBuffer parse() {
        final StringBuffer returnedBuffer = new StringBuffer();

        try {
            final File f = new File(this.inputFile);
            final InputSource input = new InputSource(new FileInputStream(f));
            // Set systemID so parser can find the dtd with a relative URL in
            // the source document.
            input.setSystemId(f.toString());

            final SAXParserFactory spfact = SAXParserFactory.newInstance();

            spfact.setValidating(true);
            spfact.setNamespaceAware(true);

            final SAXParser parser = spfact.newSAXParser();

            final XMLReader reader = parser.getXMLReader();
            // TODO WTF, instantiate a Validate inside the Validate ?

            // Instantiate inner-class error and lexical handler.
            // Validate handler = new Validate(filename, errorBuff);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            parser.parse(input, this);
            if (!this.hasDTD) {
                returnedBuffer.append("[WARNING] NO DOCTYPE DECLARATION in " + this.inputFile + "\n");
            } else if (!this.isValid()) {
                returnedBuffer.append("[ERROR] XML is NOT VALID: it does not comply to dtd in " +
                    this.inputFile + "\n");
                returnedBuffer.append("[HINT] If you have " + "trouble spotting exactly where the errors "
                    + "are, set util.DocBookize.SHORT_LINES to true.\n");
                returnedBuffer.append(this.errorBuffer);
            } else {
                returnedBuffer.append("[INFO] XML is VALID and complies to dtd (" + this.inputFile + ")\n");
            }
        } catch (final Exception e) // Any problem related to xml structure or
        // file existence
        {
            returnedBuffer.append("[ERROR] File NOT WELL-FORMED :" + this.inputFile + ". " + e.getMessage() +
                "\n");
        }

        return returnedBuffer;
    }

    /** When an error is encountered, store it for future recall */
    @Override
    public void error(final SAXParseException exc) {
        final String message = exc.getMessage();
        this.errorBuffer.append("[ERROR] " + this.inputFile + "[" + exc.getLineNumber() + "] " + message +
            "\n");

        if (message.startsWith("Element type \"") && message.endsWith("\" must be declared.")) {
            this.errorBuffer.append("[HINT] The dtd does not allow such a tag! ");
        }

        this.valid = false;
    }

    /** When a warning is encountered, store it for future recall */
    @Override
    public void warning(final SAXParseException exc) {
        this.errorBuffer.append("[WARNING] " + this.inputFile + "[" + exc.getLineNumber() + "] " +
            exc.getMessage() + "\n");
        this.valid = false;
    }

    /** Set hasDTD to true when dtd is found. */
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
        this.hasDTD = true;
    }

    public void endDTD() throws SAXException {
    }

    public void startEntity(final String name) throws SAXException {
    }

    public void endEntity(final String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void comment(final char[] ch, final int start, final int length) throws SAXException {
    }

    public boolean isValid() {
        return this.valid;
    }
}
