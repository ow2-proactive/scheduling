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
package util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * This works this way:
 * Some kind of ugly sax parsing is done on the xml file, spitting out the tags as they come,
 * until a tag <programmlisting> or <screen> is found. The data inside the tags is then
 * highlighted by the method highlight(String).  It is then fed back into the stream.
 */
public class DocBookize extends DefaultHandler {
    final static boolean DEBUG = false;
    private final String FILEPATH;
    private boolean storingCode = false;
    private String programContent;
    private BufferedWriter out;
    private String outputFileName;
    private String language = "";
    private LanguageToDocBook javaToDocBook;
    private LanguageToDocBook xmlToDocBook;

    /** @param fileName The name of the XML file which should have its <screen> tags decorated
     * @param path The path on which to find the files to include in the docbook */
    public DocBookize(String fileName, String path) {
        this.outputFileName = fileName;
        FILEPATH = path;
        this.javaToDocBook = new JavaToDocBookRegexp();
        this.xmlToDocBook = new XmlToDocBookRegexp();
    }

    public static void main(String[] argv) {
        // usage warning
        if ((argv.length != 2) && (argv.length != 1)) {
            System.err.println("Usage: docBookize filename pathForTextData");
            System.err.println(
                "Transforms <screen> and <programlisting> tags (side-effect: removes docbook indentation)");
            System.exit(-1);
        }

        System.out.println(
            "Beautifying code examples within <programlisting> tags in " +
            argv[0]);

        // Create SAX machinery 
        File inputFile = new File(argv[0]);

        // if no pathForTextData specified on command line, default is ""
        DefaultHandler handler = new DocBookize(argv[0] + ".tmp",
                ((argv.length == 2) ? argv[1] : ""));
        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser saxParser = factory.newSAXParser();
            // parse the file, using DocBookize.methods to handle tags
            saxParser.parse(inputFile, handler);

            // rename output file to first name ==> overwrite
            File resultFile = new File(argv[0] + ".tmp");
            resultFile.renameTo(inputFile);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /** Default handler operation when start document is encountered */
    public void startDocument() throws SAXException {
        // Create the output file
        try {
            out = new BufferedWriter(new FileWriter(this.outputFileName));
        } catch (IOException e) {
            throw new SAXException("I/O error opening temp file", e);
        }

        print("<?xml version='1.0' encoding='UTF-8'?>\n");
        print(
            "<!-- DocBookize has been run to highlight keywords in code examples -->\n");
    }

    /** Default handler operation when end document is encountered */
    public void endDocument() throws SAXException {
        try {
            out.close();
        } catch (IOException e) {
            throw new SAXException("I/O error closing temp file", e);
        }
    }

    /** Default handler operation when new tag is encountered */
    public void startElement(String namespaceURI, String localName,
        String realName, Attributes attrs) throws SAXException {
        String tagName = localName; // element name

        if ("".equals(tagName)) {
            tagName = realName; // namespaceAware = false
        }

        // Just skip textobjects in programlistings (see following comment).
        if (tagName.equals("textobject") && storingCode) {
            return;
        }

        // replace textdata from file by the file content (shouldn't xslt task do that?)
        if (tagName.equals("textdata") && storingCode) {
            String fileReferenced = "";

            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name 

                if ("".equals(aName)) {
                    aName = attrs.getQName(i);
                }

                if (aName.equals("fileref")) {
                    fileReferenced = attrs.getValue(i);
                }
            }

            programContent += getFileContent(fileReferenced);

            return;
        }

        print("<" + tagName);

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name 

                if (aName.equals("")) {
                    aName = attrs.getQName(i);
                }

                print(' ' + aName);
                print("=\"");
                print(attrs.getValue(i));
                print("\" ");
                
                if (aName.toLowerCase().equals("language")) {
                    this.language = attrs.getValue(i).toLowerCase();
                }

            }
        }

        print(">");

        // Only highlight programlistings.
        if (tagName.equals("programlisting")) {
            this.storingCode = true;
            programContent = "";
        }
    }

    /** Default handler operation when a tag is closed */
    public void endElement(String namespaceURI, String localName,
        String realName) throws SAXException {

        // Just skip textobjects in programlistings. 
        if (realName.toLowerCase().equals("textobject") && storingCode) {
            return;
        }
    
        // Just skip textdata END TAGS in programlistings. 
        if (realName.toLowerCase().equals("textdata") && storingCode) {
            return;
        }
    
        if (realName.toLowerCase().equals("programlisting")) {
            storingCode = false;
            highlight(programContent);
            programContent = "";
        }
    
        print("</" + realName + ">");
    }

    /** Default handler operation when a string of characters is encountered */
    public void characters(char[] buf, int offset, int len)
        throws SAXException {
        String s = new String(buf, offset, len);
        print(s.replaceAll("&", "&amp;").replaceAll("<", "&lt;"));
    }

    /** Return the content of a java file as a single String (without the first PA license comment)
     * @param fileReferenced the file which is to be eturned as a string
     * @return one very big string equal to the content of a file
     * @throws SAXException if having trouble reading the given file*/
    private String getFileContent(String fileReferenced) throws SAXException {

        String fileContent = "";

        try {
            BufferedReader in = new BufferedReader(new FileReader(FILEPATH +
                        fileReferenced));

            // Remove PA standard java file header, if any
            String str1 = in.readLine();

            if (str1 == null) {
                in.close();

                return "";
            }

            String str2 = in.readLine();

            if (str2 == null) {
                in.close();

                return str1;
            }

            if (str2.startsWith(
                        " * ################################################################")) {
                // begin PA comment, so just read it until end comment found
                do {
                    str2 = in.readLine();

                    // if EndOfFile, just return with empty String
                    if (str2 == null) {
                        return "";
                    }
                } while (!str2.endsWith("*/"));
            }
            else
                fileContent += str1 + "\n" + str2 + "\n";

            // just dump rest of the file into return value
            String str;

            while ((str = in.readLine()) != null) {
                fileContent += (str + "\n");
            }

            in.close();
        } catch (IOException e) {
            throw new SAXException("Warning - trouble reading referenced file " + fileReferenced + ": " + e.getMessage());
        }
        
        return fileContent.replaceAll("&", "&amp;").replaceAll("<", "&lt;"); 
    }

    /** Just write to the output file the given string. need to Wrap I/O exceptions in
     * SAX exceptions, to suit handler signature requirements
     * @throws SAXException if an error happened with file IO operations */
    private void print(String s) throws SAXException {
        if (storingCode) {
            programContent += s;
        } else {
            try {
                if (DEBUG) {
                    System.out.print(s);
                }

                out.write(s);
            } catch (IOException e) {
                throw new SAXException("I/O error writing to temp file", e);
            }
        }
    }

    /** Transform the given string into nice docbook highlighted stuff
     * @param s The String which is to contain tags highlighting its elements.
     * @throws SAXException if writing to the stream caused problem */
    private void highlight(String s) throws SAXException {
        
        if (s.length() > 0) {
            try {
                // Create the docbookized version of this code: 
                // First copy this code into an independent file
                File temp = File.createTempFile("db_tmp_", ".dbz");
                BufferedWriter tmpBuffer = new BufferedWriter(new FileWriter(
                            temp));
                tmpBuffer.write(s);
                tmpBuffer.close();

                // Do the highlighting
                String generated; 
                if (this.language.equals("java"))
                    generated = this.javaToDocBook.convert(temp.getPath());
                else if (this.language.equals("xml"))
                    generated = this.xmlToDocBook.convert(temp.getPath());
                else 
                    throw new SAXException("Language '"+this.language+"' is unsupported in programlistings" ); 
                temp.delete();

                // now put this code back into the xml we're generating
                BufferedReader in = new BufferedReader(new FileReader(generated));
                String str;

                while ((str = in.readLine()) != null) {
                    print(str + "\n");
                }

                in.close();
                new File(generated).delete();
                
            } catch (IOException e) {
                throw new SAXException("I/O error writing to temp file", e);
            }
        }
    }

}
