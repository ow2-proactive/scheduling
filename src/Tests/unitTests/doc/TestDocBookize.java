package unitTests.doc;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import junit.framework.Assert;

import doc.DocBookize;


/**
 * Contains tests for {@link DocBookize}.
 * 
 * @author vjuresch
 * @since 3.9
 * @version $Id$
 *
 */
public class TestDocBookize {
    /**
     * Regular expresion matching the  & lt ; and & gt ; 
     * inside programlisting tags.  
     */
    private static final String REGEXP = "(<programlisting.*>)(.*(&lt;).*|(&gt;).*?)(</programlisting>)";
    /** Path to the files cotaining xml that triggers the bug  */
    private static final String PATH = "src/Tests/unitTests/doc/";
    /** File containing the xml that triggers the bug  */
    private static final String XML_FILE = "singleLine.xml";
    /** Temporary file that will contain the modified xml   */
    private static final String TEST_FILE = "singleLineTest.xml";

    /**
     * Tests for bug PROACTIVE-346: 
     * Characters < and > do not get transformed into & lt; & gt; when code 
     * is included from programlisting tags from a file and it is a single line of code.
     * 
     * @throws Exception
     */
    @Test
    public void testTransform() throws Exception {
        final String path[] = { "./" };
        final String inPath = PATH + XML_FILE;
        final String javaSrc = "";
        final String testPath = PATH + TEST_FILE;
        //copy file to temporary file
        final InputStream input = new FileInputStream(inPath);
        final OutputStream output = new FileOutputStream(testPath);

        //copy singleLine.xml to singleLineTest.xml
        final byte[] buf = new byte[1024];
        int len;
        while ((len = input.read(buf)) > 0) {
            output.write(buf, 0, len);
        }
        input.close();
        output.close();

        //run DocBookize on singleLineTest file
        final String mainArgs[] = { testPath, javaSrc, path[0] };
        DocBookize.main(mainArgs);

        //load singleLineText.xml in  String (it's a small file)
        final File tmp = new File(testPath);
        final BufferedReader buff = new BufferedReader(new FileReader(tmp));
        String fileText = "";
        String tmpLine = "";
        while (tmpLine != null) {
            fileText += tmpLine;
            tmpLine = buff.readLine();
        }
        buff.close();
        System.out.println(fileText);
        //scan for programlisting containing < >
        //String regex = "(<programlisting.*>)(.*[><].*?)(</programlisting>)";
        //it assumes the test file is changed to contain &lt and &gt 
        final Matcher matcher = Pattern.compile(REGEXP).matcher(fileText);
        final boolean good = matcher.find();
        //delete temporary file
        if (!tmp.delete()) {
            throw new IOException("Cannot delete temporary file : " + tmp.getAbsolutePath());
        }
        Assert.assertTrue(good);
    }
}