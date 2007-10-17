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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** JavaToDocBook converter (100% java), using regular expression replacement.
 * It converts a String, representing java code, into another String,which contains
 * the java code plus docbook highlighting tags. */
public class JavaToDocBookRegexp extends RegexpHighLighter {
    final private static String signatureKeywords = "package|import|public|private|protected|static|final|class|interface|implements|extends";
    final private static String codeKeywords = "return|this|if|else|for|while|do|break|continue|try|catch|new|assert";
    final private static String typeKeywords = "void|null|int|String|Object";
    private boolean inComment;
    private boolean inString;
    private boolean inXmlTag;

    public JavaToDocBookRegexp() {
        pattern = new Pattern[] { //   \b in a regexp means a word boundary
                Pattern.compile("(\\b)(" + signatureKeywords + ")(\\b)"),
                Pattern.compile("(\\b)(" + codeKeywords + ")(\\b)"),
                Pattern.compile("(\\b)(" + typeKeywords + ")(\\b)")
            };
        replacement = new String[] {
                "$1" + OPENKEYWORD + "$2" + CLOSEKEY + "$3",
                "$1" + OPENCODE + "$2" + CLOSECODE + "$3",
                "$1" + OPENTYPE + "$2" + CLOSETYPE + "$3"
            };
        reset();
    }

    /** Add tags around java keywords.
     * @param string some java code to highlight, cannot contain \n */
    @Override
	protected String decorate(String string) {
        assert string.indexOf('\n') == -1 : "Hey, decorate can't work with newlines in the input String!";

        String result = string;

        if (this.inXmlTag) { // this tag treatment is far from perfect.

            int index = string.indexOf(">"); // for instance, it does not handle nested tags

            if (index >= 0) {
                result = (string.substring(0, index + ">".length())); // just echo the end of the tag. 
                this.inXmlTag = false;
                result += decorate(string.substring(index + ">".length()));
            } // else result=(string); already done

            return result;
        }

        if (this.inString) { // find the end of the String. (can a String span several lines?)

            int index = string.indexOf("\"");

            if (index >= 0) {
                if ((index == 0) || (string.charAt(index - 1) != '\\')) {
                    result = (string.substring(0, index + "\"".length()) +
                        CLOSESTRING);
                    this.inString = false;
                    result += decorate(string.substring(index + "\"".length()));
                } else {
                    result = (string.substring(0, index + "\"".length()));
                    result += decorate(string.substring(index + "\"".length()));
                }
            } // else  echo(string); already set 

            return result;
        }

        if (this.inComment) { // find the end of the comment

            int index = string.indexOf("*/");

            if (index >= 0) {
                result = (string.substring(0, index + "*/".length()) +
                    CLOSECOMMENT);
                this.inComment = false;
                result += decorate(string.substring(index + "*/".length()));
            } //else  echo(string); already done

            return result;
        }

        // OK, not already in tag, comment or string, so look for either of those 
        Matcher m = Pattern.compile("(.*?)(<|//|/\\*|\").*").matcher(string); //  this regexp describes the start of tag, comment or string
                                                                              //Matcher m = Pattern.compile("(<|//|/\\*|\")").matcher(string); //  this regexp describes the start of tag, comment or string

        if (!m.matches()) { // found no tag, string or comment, so just highlight the following java
                            // for all the patterns defined, do regexp replacement 

            for (int i = 0; i < this.pattern.length; i++)
                result = this.pattern[i].matcher(result)
                                        .replaceAll(this.replacement[i]);

            // pattern reuse is faster than String.replaceAll(regexp, replacement)
            return result;
        }

        int matchStart = m.start(2); // skip the first "(.*?), go to the second group instead 
        char ch = m.group().charAt(matchStart); // get this group's first character.

        switch (ch) {
        case '<': { // This is the start of an xmlTag
            result = decorate(string.substring(0, matchStart));
            result += ("<");
            this.inXmlTag = true;
            result += decorate(string.substring(matchStart + "<".length()));

            return result;
        }

        case '"': { // This is the start of a String
            result = decorate(string.substring(0, matchStart));
            result += (OPENSTRING + "\"");
            this.inString = true;
            result += decorate(string.substring(matchStart + "\"".length()));

            return result;
        }

        case '/': { // this is the start of a comment

            if (string.charAt(matchStart + 1) == '/') { // This is the start of a comment like //
                result = decorate(string.substring(0, matchStart));
                result += (OPENCOMMENT + string.substring(matchStart) +
                CLOSECOMMENT);
            } else { // This is the start of a comment like /*
                result = decorate(string.substring(0, matchStart));
                result += (OPENCOMMENT + "/*");
                this.inComment = true;
                result += decorate(string.substring(matchStart + "/*".length()));
            }

            return result;
        }

        default:
            throw new IllegalStateException(
                "Matcher said match was found, but char " +
                Character.toString(ch) + " is not in compile regexp " + string);
        }
    }

    /** Puts all variables back to their original state */
    @Override
	protected void reset() {
        this.inComment = false;
        this.inString = false;
        this.inXmlTag = false;
    }
}
