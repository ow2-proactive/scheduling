package org.objectweb.proactive.extra.ressourceallocator;

import java.io.CharArrayWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public class SimpleSchema {
    private SAXParserFactory factory; // Creates parser object
    private SAXParser parser; // Holds a parser object
    private XMLReader xmlReader; // Object that parses the file
    private DefaultHandler handler; // Defines the handler for this parser
    private boolean valid = true;

    // Set schema constants
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    public SimpleSchema() throws SAXException {
        try {
            factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            factory.setNamespaceAware(true);

            if (factory.isValidating()) {
                System.out.println("The parser is validating");
            }

            //Create Parser
            parser = factory.newSAXParser();

            // Enable Schemas
            parser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);

            //            parser.setFeature("http://apache.org/xml/features/validation/dynamic",
            //                    true);

            //Create XMLReader
            xmlReader = parser.getXMLReader();

            ContentHandler cHandler = new MyDefaultHandler();
            ErrorHandler eHandler = new MyDefaultHandler();

            xmlReader.setContentHandler(cHandler);
            xmlReader.setErrorHandler(eHandler);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void parseDocument(String xmlFile) {
        try {
            xmlReader.parse(xmlFile);
            if (valid) {
                System.out.println("Document is valid!");
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: java SimpleSchema " +
                    "[XML Document Filename]");
                System.exit(0);
            }
            SimpleSchema xmlApp = new SimpleSchema();
            xmlApp.parseDocument(args[0]);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyDefaultHandler extends DefaultHandler {
        private CharArrayWriter buff = new CharArrayWriter();
        private String errMessage = "";

        /* With a handler class, just override the methods you need to use
        */

        // Start Error Handler code here
        public void warning(SAXParseException e) {
            System.out.println("Warning Line " + e.getLineNumber() + ": " +
                e.getMessage() + "\n");
        }

        public void error(SAXParseException e) {
            errMessage = new String("Error Line " + e.getLineNumber() + ": " +
                    e.getMessage() + "\n");
            System.out.println(errMessage);
            valid = false;
        }

        public void fatalError(SAXParseException e) {
            errMessage = new String("Error Line " + e.getLineNumber() + ": " +
                    e.getMessage() + "\n");
            System.out.println(errMessage);
            valid = false;
        }
    }
}
