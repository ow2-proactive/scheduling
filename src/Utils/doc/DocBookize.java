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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;


/** This works this way:
 * Some kind of ugly sax parsing is done on the xml file, spitting out the tags as they come,
 * until a tag 'programlisting' or 'screen' is found. The data inside the tags is then
 * highlighted by the method highlight(String).  It is then fed back into the stream.
 * Also modifies 'xref' and 'textdata'. */
public class DocBookize extends DefaultHandler implements LexicalHandler {
    static String EOL = System.getProperty("line.separator");
    static boolean SHORT_LINES = false;
    private final String[] filePath; // the possible places where to look for cited files
    private String programContent = null; // when reading programlisting, stores the characters for highlighting
    private BufferedWriter out; // the output stream, ie the initial file + decoration  
    private String outputFileName; // the name of the output file
    private String htmlizedJavaPath;
    private String language = ""; // when storing programlisting, the language in which the code is written

    /** An association between 'language' ==> 'highlighter for language' */
    private Map<String, LanguageToDocBook> languageHighlighters = new HashMap<String, LanguageToDocBook>(); 
    /** fields needed to know where to add the source code at the end of the docbook xml */
    private Vector<String> listOFids = null;
    private HashMap<String, String> srcContentsToAdd = new HashMap<String, String>();
    private Vector<String> srcFilesAlreadyAdded = new Vector<String>(); 

    /** @param fileName The name of the XML file which should have its <screen> tags decorated
     * @param path The paths on which to find the files to include in the docbook */
    public DocBookize(String fileName, String htmlizedJava, String[] path) {
        this.outputFileName = fileName;
        this.htmlizedJavaPath = htmlizedJava;
        this.filePath = path; // the possible places where to look for cited files
        this.languageHighlighters.put("java", new JavaToDocBookRegexp());
        this.languageHighlighters.put("xml", new XmlToDocBookRegexp());
    }

    public static void main(String[] argv) {
        // usage warning
        if (argv.length < 2) {
            System.err.println(
                "Usage: docBookize filename pathForJavaHtmlized [pathForTextData]*");
            System.err.println(
                "Transforms <programlisting> tags (side-effect: removes docbook indentation)");
            System.exit(-1);
        }

        String fileToBeautify = argv[0];
        String htmlizedJava = argv[1];
        System.out.println(
            "[INFO] Beautifying code examples within <programlisting> tags (" +
            fileToBeautify + ")");

        // Create SAX machinery 
        File inputFile = new File(fileToBeautify);

        //path to look for files = argv - (2 first occurrence) + (inputFile Directory)
        String[] path = new String[argv.length - 1];
        path[0] = inputFile.getParent() + "/"; // the directory of file to beautify
        System.arraycopy(argv, 2, path, 1, argv.length - 2);

        DocBookize handler = new DocBookize(fileToBeautify + ".tmp",
                htmlizedJava, path);

        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setNamespaceAware(true);

        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader()
                     .setProperty("http://xml.org/sax/properties/lexical-handler",
                handler);
            // parse the file, using DocBookize.methods to handle tags
            saxParser.parse(inputFile, handler);

            // rename output file to first name ==> overwrite
            File resultFile = handler.getOutputFile();

            if (!inputFile.delete()) {
                throw new IOException("Could not delete file " + inputFile +
                    ".");
            }

            if (!resultFile.renameTo(inputFile)) {
                throw new IOException("Could not rename file " + resultFile +
                    " to " + inputFile + ".");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /** Default handler operation when start document is encountered */
    @Override
	public void startDocument() throws SAXException {
        // Create the output file
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(this.outputFileName), "UTF8"));
        } catch (IOException e) {
            throw new SAXException("I/O error opening temp file", e);
        }

        print("<?xml version='1.0' encoding='UTF-8'?>" + EOL);
        print(
            "<!-- DocBookize has been run to highlight keywords in code examples -->" +
            EOL);
    }

    /** Default handler operation when end document is encountered */
    @Override
	public void endDocument() throws SAXException {
        try {
            out.close();
        } catch (IOException e) {
            throw new SAXException("I/O error closing temp file", e);
        }
    }

    /** Default handler operation when new tag is encountered */
    @Override
	public void startElement(String namespaceURI, String localName,
        String realName, Attributes attrs) throws SAXException {
        String tagName = localName; // element name
        boolean filerefChanged = false;

        if ("".equals(tagName)) {
            tagName = realName; // namespaceAware = false
        }

        // Just skip textobjects in programlistings (see following comment).
        if (tagName.equals("textobject") && (this.programContent != null)) {
            return;
        }

        // replace textdata from file by the file content (shouldn't the ant xslt task do that?)
        if (tagName.equals("textdata") && (this.programContent != null)) {
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

            this.programContent += getFileContent(fileReferenced);

            return;
        }

        // XREF with role=xmlFileSrc or javaFileSrc are wraped to add the correct references.  
        if (tagName.equals("xref") && (attrs != null)) {
            String fileRef = "";
            String role = "";

            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i).toLowerCase(); // Attr name 

                if (aName.equals("")) {
                    aName = attrs.getQName(i).toLowerCase();
                }

                if (aName.equals("linkend")) {
                    filerefChanged = true;
                    fileRef = attrs.getValue(i);
                }

                if (aName.equals("role")) {
                    role = attrs.getValue(i);
                }
            }

            if (!role.equals("") && !fileRef.equals("")) {
                this.language = fileRef.substring(fileRef.lastIndexOf(".") + 1)
                                       .toLowerCase();

                if (!this.srcFilesAlreadyAdded.contains(fileRef)) { // We shall only add the same file once!
                    this.srcFilesAlreadyAdded.add(fileRef);

                    String listing = new String();
                    listing += ("<example id=\"" +
                    fileRef.replaceAll("/", ".") + "\">");
                    listing += (" <title> ");

                    // In html, just point to the file, wherever it is. 
                    String location = fileRef;

                    if (role.equals("javaFileSrc")) {
                        location = htmlizedJavaPath + fileRef + ".html";
                    }

                    listing += (" <phrase os=\"html\" > <ulink url=\"../" +
                    location + "\"> " + fileRef + "</ulink></phrase>");

                    // in pdf, copy the file to the docbook xml
                    listing += (" <phrase os=\"pdf\" > " + fileRef +
                    "</phrase>");
                    listing += (" </title> ");
                    listing += (" <programlisting os=\"pdf\" lang=\"" +
                    this.language + "\">");
                    listing += (highlight(getFileContent(fileRef)));
                    listing += (" </programlisting>");
                    listing += (" <para os=\"html\" /> "); // don't leave the example empty if using html
                    listing += ("</example>");

                    if (this.srcContentsToAdd.containsKey(role)) {
                        String old = this.srcContentsToAdd.remove(role);
                        this.srcContentsToAdd.put(role, old + listing);
                    } else {
                        this.srcContentsToAdd.put(role, listing);
                    }
                }
            }
        } // keep on with startElement method, because we haven't even printed the <xref> tag!,

        print("<" + tagName);

        String id = null;

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name 

                if (aName.equals("")) {
                    aName = attrs.getQName(i);
                }

                print(' ' + aName);
                print("=\"");

                if (filerefChanged && aName.equals("linkend")) {
                    print(attrs.getValue(i).replaceAll("/", "."));
                } else {
                    print(attrs.getValue(i));
                }

                print("\"");

                if (aName.toLowerCase().equals("lang")) {
                    this.language = attrs.getValue(i).toLowerCase();
                }

                if (aName.toLowerCase().equals("url") &&
                        !attrs.getValue(i).startsWith("http")) {
                }

                if (aName.equals("id")) {
                    id = attrs.getValue(i);
                }
            }
        }

        if ("srcCodeAppendix".equals(id)) {
            assert this.listOFids == null : "Hey, listOfIds should have been null, meaning 'not yet found srcCodeAppendix'";
            this.listOFids = new Vector<String>(); // used to avoid searching in srcContentsToAdd too often.
        }

        if (this.listOFids != null) {
            this.listOFids.add(id); // just remembering the current id if there is one.  
        }

        print(">");

        // Only highlight programlistings.
        if (tagName.equals("programlisting")) {
            this.programContent = ""; // start storing the next characters, until en dof programlisting
        }
    }

    /** Default handler operation when a tag is closed */
    @Override
	public void endElement(String namespaceURI, String localName,
        String realName) throws SAXException {
        // Code to add remembered elements in the src appendix 
        if (this.listOFids != null) {
            String id = this.listOFids.remove(this.listOFids.size() -
                    1);

            if (this.listOFids.size() == 0) { // we have stepped out of the appendix
                this.listOFids = null;
            }

            if (this.srcContentsToAdd.containsKey(id)) { // end of a tag labelled to have extra info? ==> paste this extra info
                print((this.srcContentsToAdd.get(id)).toString());
            }
        }

        // Just skip textobjects in programlistings. 
        if (realName.toLowerCase().equals("textobject") &&
                (this.programContent != null)) {
            return;
        }

        // Just skip textdata END TAGS in programlistings. 
        if (realName.toLowerCase().equals("textdata") &&
                (this.programContent != null)) {
            return;
        }

        if (realName.toLowerCase().equals("programlisting")) {
            String highlighted = highlight(this.programContent);
            this.programContent = null; // No longer in a programlisting tag ==> no longer have to store characters
            print(highlighted);
        }

        // print also a newline to have reasonably lengthed lines but some scrren listings will be ill-formed.   
        if (SHORT_LINES) {
            print("</" + realName + ">" + EOL);
        } else {
            print("</" + realName + ">");
        }
    }

    /** Default handler operation when a string of characters is encountered */
    @Override
	public void characters(char[] buf, int offset, int len)
        throws SAXException {
        String s = new String(buf, offset, len);
        print(s.replaceAll("&", "&amp;").replaceAll("<", "&lt;"));
    }

    /** This Parser writes to an output file.
     * @return the File which has been created. */
    private File getOutputFile() {
        return new File(this.outputFileName);
    }

    /** Return the content of a file as a single String (without the first PA license comment, for java files)
     * @param fileReferenced the file which is to be returned as a string
     * @return one very big string equal to the content of the file
     * @throws SAXException if having trouble reading the given file*/
    private String getFileContent(String fileReferenced)
        throws SAXException {
        String fileContent = "";

        BufferedReader in = null;

        // try to find the cited file, on the allowed paths
        String fullFileName = "";

        for (int i = 0; i < this.filePath.length; i++) {
            fullFileName = this.filePath[i] + fileReferenced;

            if (new File(fullFileName).isFile()) { // it's not a file when it doesn't exist or it is a directory

                break; // file found ? Cool, we've got our file!
            }
        }

        try {
            in = new BufferedReader(new FileReader(fullFileName));
        } catch (FileNotFoundException e1) {
            System.err.println("[WARNING] Couldn't find file called " +
                fileReferenced);

            return "XXXXXXXXXXXX  " + fileReferenced +
            " missing  XXXXXXXXXXXX";
        }

        try {
            // Remove PA standard java file header, if any. This means reading the first two lines
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
            } else {
                fileContent += (str1 + EOL + str2 + EOL);
            }

            // just dump rest of the file into the return value
            String str;

            while ((str = in.readLine()) != null) {
                fileContent += (str + EOL);
            }

            in.close();
        } catch (IOException e) {
            throw new SAXException("Warning - trouble reading referenced file " +
                fileReferenced + ": " + e.getMessage());
        }

        return fileContent.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
    }

    /** Just write to the output file the given string. Need to Wrap I/O exceptions in
     * SAX exceptions, to suit handler signature requirements.
     * @throws SAXException if an error happened with file IO operations */
    private void print(String s) throws SAXException {
        if (this.programContent != null) {
            this.programContent += s;
        } else {
            try {
                out.write(s);
            } catch (IOException e) {
                throw new SAXException("I/O error writing to temp file", e);
            }
        }
    }

    /** Transform the given string into nice docbook highlighted stuff
     * @param s The String which is to contain tags highlighting its elements.
     * @throws SAXException if writing to the stream caused problem */
    private String highlight(String s) {
        String result = "";

        if (s.length() > 0) {
            LanguageToDocBook converter = this.languageHighlighters.get(this.language);

            if (converter == null) {
                System.err.println("Language '" + this.language +
                    "' is unsupported in programlistings.");
                result = s;
            } else { // Do the highlighting 
                result = converter.convert(s);
            }
        }

        return result;
    }

    public void startDTD(String baseElement, String publicId, String systemId)
        throws SAXException {
        // also include in the output the dtd in original file.
        print("<!DOCTYPE " + baseElement + " PUBLIC \"" + publicId + "\" \"" +
            systemId + "\">" + EOL);
    }

    public void endDTD() throws SAXException {
    }

    public void comment(char[] s, int i, int j) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void startEntity(String ent) throws SAXException {
    }

    public void endEntity(String ent) throws SAXException {
    }
}
