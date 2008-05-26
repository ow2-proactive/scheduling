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
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This works this way: Some kind of ugly sax parsing is done on the xml file,
 * spitting out the tags as they come, until a tag 'programlisting' or 'screen'
 * is found. The data inside the tags is then highlighted by the method
 * highlight(String). It is then fed back into the stream. Also modifies 'xref'
 * and 'textdata'.
 */
public class DocBookize extends DefaultHandler implements LexicalHandler {
    private static final String SRC_CODE_APPENDIX = "srcCodeAppendix";
    private static final Logger logger = Logger.getLogger(DocBookize.class.getName());
    private static final String UTF8 = "UTF8";
    private static final String TMP_EXTENSION = ".tmp";
    private static final String XML = "xml";
    private static final String JAVA = "java";
    private static final String LT = "&lt;";
    private static final String AMPERSAND_REPLACE = "&amp;";
    private static final String AMPERSAND = "&";
    private static final String TAG_START = "<";
    private static final String TAG_CLOSE = ">";
    private static final String END_TAG_BEGIN = "</";
    private static final String PROGRAMLISTING = "programlisting";
    private static final String ID = "xml:id";
    private static final String HTTP = "http";
    private static final String URL = "url";
    private static final String LANG = "xml:lang";
    private static final String EQUAL_QUOTES = "=\"";
    private static final String QUOTES = "\"";
    private static final String EXAMPLE_END = "</example>";
    private static final String PARA_OS_HTML = " <para os=\"html\" /> ";
    private static final String PROGRAMLISTING_END = " </programlisting>";
    private static final String TAG_END = "\">";
    private static final String PROGRAMLISTING_OS_PDF_LANG = " <programlisting os=\"pdf\" xml:lang=\"";
    private static final String TITLE_END = " </title> ";
    private static final String PHRASE_END = "</phrase>";
    private static final String PHRASE_OS_PDF = " <phrase os=\"pdf\" > ";
    private static final String ULINK_PHRASE = "</uri></phrase>";
    private static final String PHRASE_OS_HTML_ULINK_URL = " <phrase os=\"html\" > "
        + "<uri xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"../";
    private static final String TITLE_START = " <title> ";
    private static final String EXAMPLE_ID_START = "<example xml:id=\"";
    private static final String HTML_EXT = ".html";
    private static final String JAVA_FILE_SRC = "javaFileSrc";
    private static final String XREF = "xref";
    private static final String FILEREF = "fileref";
    private static final String TEXTDATA = "textdata";
    private static final String TEXTOBJECT = "textobject";
    private static final String DOC_RUN_COMMENT = "<!-- DocBookize has been run to highlight keywords in code examples -->";
    private static final String XML_ENCODING = "<?xml version='1.0' encoding='UTF-8'?>";
    private static final String SAX_PROPERTIES = "http://xml.org/sax/properties/lexical-handler";
    private static final String ROLE = "role";
    private static final String LINKEND = "linkend";
    private static final String EOL = System.getProperty("line.separator");
    private static final boolean SHORT_LINES = false;
    /** places where to look for cited files */
    private final String[] filePath;
    /** when reading programlisting, stores the characters for highlighting */
    private String programContent;
    /** the output stream, ie the initial file decoration */
    private BufferedWriter out;
    /** the name of the output file */
    private final String outputFileName;
    private final String htmlizedJavaPath;
    /** when storing programlisting, the language in which the code is written */
    private String language = "";
    /** An association between 'language' ==> 'highlighter for language' */
    private final Map<String, LanguageToDocBook> languageHighlighters = new HashMap<String, LanguageToDocBook>();
    /**
     * fields needed to know where to add the source code at the end of the
     * docbook xml
     */
    private List<String> listOFids;
    private final Map<String, String> srcContentsToAdd = new HashMap<String, String>();
    private final List<String> srcFilesAlreadyAdded = new Vector<String>();

    /**
     * @param fileName
     * 				The name of the XML file which should have its <screen> tags
     *            	decorated
     * @param htmlizedJava
     * @param path
     * 			The paths on which to find the files to include in the docbook
     */
    public DocBookize(final String fileName, final String htmlizedJava, final String[] path) {
        //TODO do external 
        DocBookize.logger.setLevel(Level.INFO);
        this.outputFileName = fileName;
        this.htmlizedJavaPath = htmlizedJava;
        // the possible places where to look for cited files
        this.filePath = path.clone();
        this.languageHighlighters.put(DocBookize.JAVA, new JavaToDocBookRegexp());
        this.languageHighlighters.put(DocBookize.XML, new XmlToDocBookRegexp());
    }

    public static void main(final String[] argv) {
        // usage warning
        if (argv.length < 2) {
            DocBookize.logger.error("Usage: docBookize filename " + "pathForJavaHtmlized [pathForTextData]*");
            DocBookize.logger.error("Transforms <programlisting> tags "
                + "(side-effect: removes docbook indentation)");
            return;
        }

        final String fileToBeautify = argv[0];
        final String htmlizedJava = argv[1];
        DocBookize.logger.info("Beautifying code examples within" + " <programlisting> tags (" +
            fileToBeautify + ")");

        // Create SAX machinery
        final File inputFile = new File(fileToBeautify);

        // path to look for files = argv - (2 first occurrence) + (inputFile
        // Directory)
        final String[] path = new String[argv.length - 1];
        // the directory of file to beautify
        path[0] = inputFile.getParent() + "/";
        System.arraycopy(argv, 2, path, 1, argv.length - 2);

        final DocBookize handler = new DocBookize(fileToBeautify + DocBookize.TMP_EXTENSION, htmlizedJava,
            path);

        final SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setNamespaceAware(true);

        try {
            final SAXParser saxParser = factory.newSAXParser();
            saxParser.getXMLReader().setProperty(DocBookize.SAX_PROPERTIES, handler);
            // parse the file, using DocBookize.methods to handle tags
            saxParser.parse(inputFile, handler);

            // rename output file to first name ==> overwrite
            final File resultFile = handler.getOutputFile();

            if (!inputFile.delete()) {
                throw new IOException("Could not delete file " + inputFile + ".");
            }

            if (!resultFile.renameTo(inputFile)) {
                throw new IOException("Could not rename file " + resultFile + " to " + inputFile + ".");
            }
        } catch (final IOException t) {
            DocBookize.logger.error(t.getMessage(), t);
        } catch (final SAXNotRecognizedException e) {
            DocBookize.logger.error(e.getMessage(), e);
        } catch (final SAXNotSupportedException e) {
            DocBookize.logger.error(e.getMessage(), e);
        } catch (final SAXException e) {
            DocBookize.logger.error(e.getMessage(), e);
        } catch (final ParserConfigurationException e) {
            DocBookize.logger.error(e.getMessage(), e);
        }
    }

    /**
     * Creates the output file and inserts the  XML_ENCODING and DOC_RUN_COMMENT 
     * 
     * @throws SAXException 
     * */
    @Override
    public void startDocument() throws SAXException {
        try {
            this.out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFileName),
                DocBookize.UTF8));
            this.writeToOutputFile(DocBookize.XML_ENCODING + DocBookize.EOL);
            this.writeToOutputFile(DocBookize.DOC_RUN_COMMENT + DocBookize.EOL);
        } catch (final UnsupportedEncodingException e) {
            DocBookize.logger.error(e.getMessage(), e);
        } catch (final FileNotFoundException e) {
            DocBookize.logger.error("Cannot create file: " + this.outputFileName + "\n" + e.getMessage(), e);
        }

    }

    /** Default handler operation when end document is encountered */
    @Override
    public void endDocument() throws SAXException {
        try {
            this.out.close();
        } catch (final IOException e) {
            DocBookize.logger.error("Could not close file: " + this.out + "\n" + e.getMessage(), e);
        }
    }

    /** Default handler operation when new tag is encountered.
     * Process the tags localName according to the tag type.
     * If the tags is TEXTOBJECT it just returns.
     * */
    @Override
    public void startElement(final String namespaceURI, final String localName, final String realName,
            final Attributes attrs) throws SAXException {
        DocBookize.logger.debug("Processing local name [ " + localName + " ] real name [ " + realName + " ]");
        // element name
        String tagName = localName;
        boolean filerefChanged = false;

        if (tagName.isEmpty()) {
            // namespaceAware = false
            tagName = realName;
        }

        // Just skip textobjects in programlistings (see following comment).
        if (tagName.equals(DocBookize.TEXTOBJECT) && (this.programContent != null)) {
            return;
        }

        // replace textdata from file by the file content (shouldn't the ant
        // xslt task do that?)
        if (tagName.equals(DocBookize.TEXTDATA) && (this.programContent != null)) {
            String fileReferenced = "";
            //iterate through the attributes and search for a FILEREF attribute
            for (int i = 0; i < attrs.getLength(); i++) {
                // Attr name
                String aName = attrs.getLocalName(i);
                DocBookize.logger.debug("Processing attribute:" + aName);
                if (aName.isEmpty()) {
                    aName = attrs.getQName(i);
                }
                //there can be only FILEREF attribute in the tag
                if (aName.equals(DocBookize.FILEREF)) {
                    fileReferenced = attrs.getValue(i);
                }
            }

            this.programContent += this.getFileContent(fileReferenced);

            return;
        }

        // XREF with role=xmlFileSrc or javaFileSrc are wraped to add the
        // correct references.
        if (tagName.equals(DocBookize.XREF) && (attrs != null)) {
            String fileRef = "";
            String role = "";

            for (int i = 0; i < attrs.getLength(); i++) {
                // Unqualified attribute name
                String aName = attrs.getLocalName(i).toLowerCase();

                if (aName.isEmpty()) {
                    aName = attrs.getQName(i).toLowerCase();
                }

                if (aName.equals(DocBookize.LINKEND)) {
                    filerefChanged = true;
                    fileRef = attrs.getValue(i);
                }

                if (aName.equals(DocBookize.ROLE)) {
                    role = attrs.getValue(i);
                }
            }

            if (!role.isEmpty() && !fileRef.isEmpty()) {
                this.language = fileRef.substring(fileRef.lastIndexOf('.') + 1).toLowerCase();
                // We shall only add the same file once!
                // checks if the file has already been used and if not
                // adds to the lists of files added
                if (!this.srcFilesAlreadyAdded.contains(fileRef)) {
                    this.srcFilesAlreadyAdded.add(fileRef);

                    StringBuffer listing = new StringBuffer();
                    listing.append(DocBookize.EXAMPLE_ID_START + fileRef.replaceAll("/", ".") +
                        DocBookize.TAG_END);
                    listing.append(DocBookize.TITLE_START);

                    // In html, just point to the file, wherever it is.
                    String location = fileRef;

                    if (role.equals(DocBookize.JAVA_FILE_SRC)) {
                        location = this.htmlizedJavaPath + fileRef + DocBookize.HTML_EXT;
                    }

                    listing.append(DocBookize.PHRASE_OS_HTML_ULINK_URL + location + DocBookize.TAG_END +
                        fileRef + DocBookize.ULINK_PHRASE);

                    // in pdf, copy the file to the docbook xml
                    listing.append(DocBookize.PHRASE_OS_PDF + fileRef + DocBookize.PHRASE_END);
                    listing.append(DocBookize.TITLE_END);
                    listing
                            .append(DocBookize.PROGRAMLISTING_OS_PDF_LANG + this.language +
                                DocBookize.TAG_END);
                    listing.append(this.highlight(this.getFileContent(fileRef)));
                    listing.append(DocBookize.PROGRAMLISTING_END);
                    // don't leave the example empty if using html
                    listing.append(DocBookize.PARA_OS_HTML);
                    listing.append(DocBookize.EXAMPLE_END);

                    if (this.srcContentsToAdd.containsKey(role)) {
                        final String old = this.srcContentsToAdd.remove(role);
                        this.srcContentsToAdd.put(role, old + listing);
                    } else {
                        this.srcContentsToAdd.put(role, listing.toString());
                    }
                }
            }
        } // keep on with startElement method, because we haven't even printed
        // the <xref> tag!,

        this.writeToOutputFile(DocBookize.TAG_START + tagName);

        String id = null;

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                // Attr name
                final String aName = attrs.getQName(i);

                this.writeToOutputFile(' ' + aName);
                this.writeToOutputFile(DocBookize.EQUAL_QUOTES);

                if (filerefChanged && aName.equals(DocBookize.LINKEND)) {
                    this.writeToOutputFile(attrs.getValue(i).replaceAll("/", "."));
                } else {
                    this.writeToOutputFile(attrs.getValue(i));
                }

                this.writeToOutputFile(DocBookize.QUOTES);

                if (aName.equalsIgnoreCase(DocBookize.LANG)) {
                    this.language = attrs.getValue(i).toLowerCase();
                }

                if (aName.equalsIgnoreCase(DocBookize.URL) && !attrs.getValue(i).startsWith(DocBookize.HTTP)) {
                }

                if (aName.equals(DocBookize.ID)) {
                    id = attrs.getValue(i);
                }
            }
        }

        if (SRC_CODE_APPENDIX.equals(id)) {
            assert this.listOFids == null : "Hey, listOfIds should have"
                + " been null, meaning 'not yet found srcCodeAppendix'";
            // used to avoid searching in srcContentsToAdd too often.
            this.listOFids = new Vector<String>();
        }

        if (this.listOFids != null) {
            // just remembering the current id if there is one.
            this.listOFids.add(id);
        }

        this.writeToOutputFile(DocBookize.TAG_CLOSE);

        // Only highlight programlistings.
        if (tagName.equals(DocBookize.PROGRAMLISTING)) {
            // start storing the next characters, until en dof programlisting
            this.programContent = "";
        }
    }

    /** Default handler operation when a tag is closed */
    @Override
    public void endElement(final String namespaceURI, final String localName, final String realName)
            throws SAXException {
        // Code to add remembered elements in the src appendix
        if (this.listOFids != null) {
            final String id = this.listOFids.remove(this.listOFids.size() - 1);
            // we have stepped out of the appendix
            if (this.listOFids.size() == 0) {
                this.listOFids = null;
            }
            // end of a tag labelled to have extra info? ==> paste this extra
            // info
            if (this.srcContentsToAdd.containsKey(id)) {
                this.writeToOutputFile(this.srcContentsToAdd.get(id));
            }
        }

        // Just skip textobjects in programlistings.
        if (realName.equalsIgnoreCase(DocBookize.TEXTOBJECT) && (this.programContent != null)) {
            return;
        }

        // Just skip textdata END TAGS in programlistings.
        if (realName.equalsIgnoreCase(DocBookize.TEXTDATA) && (this.programContent != null)) {
            return;
        }

        if (realName.equalsIgnoreCase(DocBookize.PROGRAMLISTING)) {
            final String highlighted = this.highlight(this.programContent);
            // No longer in a programlisting tag ==> no longer have to store
            // characters
            this.programContent = null;
            this.writeToOutputFile(highlighted);
        }

        // print also a newline to have reasonably lengthed lines but some
        // scrren listings will be ill-formed.
        if (DocBookize.SHORT_LINES) {
            this.writeToOutputFile(DocBookize.END_TAG_BEGIN + realName + DocBookize.TAG_CLOSE +
                DocBookize.EOL);
        } else {
            this.writeToOutputFile(DocBookize.END_TAG_BEGIN + realName + DocBookize.TAG_CLOSE);
        }
    }

    /** Default handler operation when a string of characters is encountered */
    @Override
    public void characters(final char[] buf, final int offset, final int len) throws SAXException {
        final String s = new String(buf, offset, len);
        this.writeToOutputFile(s.replaceAll(DocBookize.AMPERSAND, DocBookize.AMPERSAND_REPLACE).replaceAll(
                DocBookize.TAG_START, DocBookize.LT));
    }

    /**
     * This Parser writes to an output file.
     * 
     * @return the File which has been created.
     */
    private File getOutputFile() {
        return new File(this.outputFileName);
    }

    /**
     * Return the content of a file as a single String (without the first PA
     * license comment, for java files)
     * 
     * @param fileReferenced
     *            the file which is to be returned as a string
     * @return one very big string equal to the content of the file
     * @throws SAXException
     *             if having trouble reading the given file
     */
    private String getFileContent(final String fileReferenced) throws SAXException {
        StringBuffer fileContent = new StringBuffer();

        BufferedReader in = null;

        // try to find the cited file, on the allowed paths
        String fullFileName = "";
        for (int i = 0; i < this.filePath.length; i++) {
            fullFileName = this.filePath[i] + fileReferenced;
            // it's not a file when it doesn't exist or it is a directory
            if (new File(fullFileName).isFile()) {
                // file found ? Cool, we've got our file!
                break;
            }
        }

        try {
            in = new BufferedReader(new FileReader(fullFileName));
            // Remove PA standard java file header, if any. This means reading
            // the first two lines
            final String str1 = in.readLine();

            if (str1 == null) {
                in.close();

                return "";
            }

            String str2 = in.readLine();

            if (str2 == null) {
                in.close();

                return str1;
            }

            if (str2.startsWith(" * #########################" + "#######################################")) {
                // begin PA comment, so just read it until end comment found
                do {
                    str2 = in.readLine();

                    // if EndOfFile, just return with empty String
                    if (str2 == null) {
                        in.close();
                        return "";
                    }
                } while (!str2.endsWith("*/"));
            } else {
                fileContent.append(str1 + DocBookize.EOL + str2 + DocBookize.EOL);
            }

            // just dump rest of the file into the return value
            String str;

            while ((str = in.readLine()) != null) {
                fileContent.append(str + DocBookize.EOL);
            }

            in.close();
            return fileContent.toString().replaceAll(DocBookize.AMPERSAND, DocBookize.AMPERSAND_REPLACE)
                    .replaceAll(DocBookize.TAG_START, DocBookize.LT);
        } catch (final FileNotFoundException e1) {
            DocBookize.logger.info("Could not read file: " + fullFileName);
            DocBookize.logger.debug("Could not find file: " + fullFileName, e1);
        } catch (final IOException e) {
            DocBookize.logger.info("Could not read or write to file: " + fullFileName);
            DocBookize.logger.debug("Could not read or write to file: " + fullFileName, e);
        }
        return null;
    }

    /**
     * Writes to the output file the given string. Need to Wrap I/O
     * exceptions in SAX exceptions, to suit handler signature requirements.
     * 
     * @param s the string to be written to the output file
     * @throws SAXException
     *             if an error happened with file IO operations
     */
    private void writeToOutputFile(final String s) throws SAXException {
        if (this.programContent != null) {
            this.programContent += s;
        } else {
            try {
                this.out.write(s);
            } catch (final IOException e) {
                throw new SAXException("I/O error writing to temp file", e);
            }
        }
    }

    /**
     * Transform the given string into nice highlighted docbook.
     * 
     * @param s
     *            The String which is to contain tags highlighting its elements.
     * @throws SAXException
     *             if writing to the stream caused problem
     */
    private String highlight(final String s) {
        String result = "";

        if (s.length() > 0) {
            final LanguageToDocBook converter = this.languageHighlighters.get(this.language);

            if (converter == null) {
                DocBookize.logger
                        .error("Language '" + this.language + "' is unsupported in programlistings.");
                result = s;
                // Do the highlighting
            } else {
                result = converter.convert(s);
            }
        }

        return result;
    }

    public void startDTD(final String baseElement, final String publicId, final String systemId)
            throws SAXException {
        // also include in the output the dtd in original file.
        this.writeToOutputFile("<!DOCTYPE " + baseElement + " PUBLIC \"" + publicId + "\" \"" + systemId +
            DocBookize.TAG_END + DocBookize.EOL);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(final char[] s, final int i, final int j) throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    public void startEntity(final String ent) throws SAXException {
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    public void endEntity(final String ent) throws SAXException {
    }
}
