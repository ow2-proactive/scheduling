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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** JavaToDocBook converter (100% java), using regular expression replacement.
 * It converts a String, pointing to a file of java code, into another String,
 * which in turn points to a new file containing the java code plus docbook highlighting tags. */
public class JavaToDocBookRegexp implements LanguageToDocBook {

    final private static String signatureKeywords = "package|import|public|private|protected|static|final|class|interface|implements|extends";
    final private static String codeKeywords = "return|this|if|else|for|while|do|break|continue|try|catch|new|assert";
    final private static String typeKeywords = "void|null|int|String|Object";

    //   \b in a regexp means a word boundary                     
    final private static Pattern[] pattern = {
            Pattern.compile("(\\b)(" + signatureKeywords + ")(\\b)"),
            Pattern.compile("(\\b)(" + codeKeywords + ")(\\b)"),
            Pattern.compile("(\\b)(" + typeKeywords + ")(\\b)")
        };
    final private static String[] replacement = {
            "$1" + OPENKEYWORD + "$2" + CLOSEKEY + "$3",
            "$1" + OPENCODE + "$2" + CLOSECODE + "$3",
            "$1" + OPENTYPE + "$2" + CLOSETYPE + "$3"
        };
    
    /**  this regexp describes the start of tag, comment or string */
    final private static Pattern specialStart = Pattern.compile("(.*?)(<|//|/\\*|\").*"); 

    private boolean inComment = false;
    private boolean inString = false;
    private BufferedWriter tmpBuffer;
    private boolean inXmlTag;

    /** Convert the given file into docbook highlighted code.
     * @param path the place from where should be loaded the file to convert
     * @return the file which is created, with highlighted tags. */
    public String convert(String path) throws IOException {
        // create file in which we shall output the code.
        File temp = new File(path + ".xml");
        tmpBuffer = new BufferedWriter(new FileWriter(temp));

        // read all strings from given file, and decorate
        BufferedReader in = new BufferedReader(new FileReader(path));
        String str;

        while ((str = in.readLine()) != null) {
            //decorate(str.replaceAll(" ", "&nbsp;")+"\n");
            decorate(str );
            echo("\n");
        }

        in.close();
        tmpBuffer.close();

        return path + ".xml";
    }

    /** Add tags around java keywords */
    private void decorate(String string) throws IOException {
        if (this.inXmlTag) { // this tag treatment is far from perfect.

            int index = string.indexOf(">"); // for instance, it does not handle nested tags

            if (index >= 0) {
                echo(string.substring(0, index + ">".length())); // just echo the end of the tag. 
                this.inXmlTag = false;
                decorate(string.substring(index + ">".length()));
            } else {
                echo(string);
            }

            return;
        }

        if (this.inString) { // find the end of the String. (can a String span several lines?)

            int index = string.indexOf("\"");

            if (index >= 0) {
                if ((index == 0) || (string.charAt(index - 1) != '\\')) {
                    echo(string.substring(0, index + "\"".length()) +
                        CLOSESTRING);
                    this.inString = false;
                    decorate(string.substring(index + "\"".length()));
                } else {
                    echo(string.substring(0, index + "\"".length()));
                    decorate(string.substring(index + "\"".length()));
                }
            } else {
                echo(string);
            }

            return;
        }

        if (this.inComment) { // find the end of the comment

            int index = string.indexOf("*/");

            if (index >= 0) {
                echo(string.substring(0, index + "*/".length()) + CLOSECOMMENT);
                this.inComment = false;
                decorate(string.substring(index + "*/".length()));
            } else {
                echo(string);
            }

            return;
        }

        // OK, not already in tag, comment or string, so look for either of those 
        Matcher m = specialStart.matcher(string);

        if (!m.matches()) {
            // found no tag, string or comment, so just highlight the following java
            String result = string;

            // for all the patterns defined, do regexp replacement 
            for (int i = 0; i < pattern.length; i++)
                result = pattern[i].matcher(result).replaceAll(replacement[i]);
                // pattern reuse is faster than String.replaceAll(regecp, replacement)
            
            echo(result);

            return;
        }

        int matchStart  = m.start(2);  // skip the first "(.*?), go to the second group instead 
        char ch = m.group().charAt(matchStart);     // get this group's first character.

        if (ch == '<') { // This is the start of an xmlTag
            decorate(string.substring(0, matchStart));
            echo("<");
            inXmlTag = true;
            decorate(string.substring(matchStart + "<".length()));

            return;
        }

        if (ch == '"') { // This is the start of a String
            decorate(string.substring(0, matchStart));
            echo(OPENSTRING + "\"");
            inString = true;
            decorate(string.substring(matchStart + "\"".length()));

            return;
        }

        if (ch == '/') {         
            if (string.charAt(matchStart + 1) == '/') { // This is the start of a comment like //
                decorate(string.substring(0, matchStart));
                echo(OPENCOMMENT + string.substring(matchStart) + CLOSECOMMENT);
            } else { // This is the start of a comment like /*
                decorate(string.substring(0, matchStart));
                echo(OPENCOMMENT + "/*");
                inComment = true;
                decorate(string.substring(matchStart + "/*".length()));
            }

            return;
        }
        
        throw new IllegalStateException("Matcher said match was found, but char " + Character.toString(ch) + " is not in compile regexp "  + string);
    }

    private void echo(String s) throws IOException {
        tmpBuffer.write(s.replaceAll("\n", EOL));
    }

    /** Check that this can really work.
     * @return true in all cases, because this is 100% java, regular expression-based word replacement  */
    public boolean willWork() {
        return true;
    }
}
