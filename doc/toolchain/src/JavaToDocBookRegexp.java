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
        //   \b in a regexp means a word boundary
        pattern = new Pattern[] { Pattern.compile("(\\b)(" + signatureKeywords + ")(\\b)"),
                Pattern.compile("(\\b)(" + codeKeywords + ")(\\b)"),
                Pattern.compile("(\\b)(" + typeKeywords + ")(\\b)") };
        replacement = new String[] { "$1" + OPENKEYWORD + "$2" + CLOSEKEY + "$3",
                "$1" + OPENCODE + "$2" + CLOSECODE + "$3", "$1" + OPENTYPE + "$2" + CLOSETYPE + "$3" };
        this.reset();
    }

    /** Add tags around java keywords.
     * @param string some java code to highlight, cannot contain \n */
    @Override
    protected String decorate(final String string) {
        assert string.indexOf('\n') == -1 : "Hey, decorate can't work with newlines in the input String!";

        String result = string;
        // this tag treatment is far from perfect.
        if (this.inXmlTag) {
            // for instance, it does not handle nested tags
            final int index = string.indexOf(">");

            if (index >= 0) {
                // just echo the end of the tag.
                result = string.substring(0, index + ">".length());
                this.inXmlTag = false;
                result += this.decorate(string.substring(index + ">".length()));
            } // else result=(string); already done

            return result;
        }
        // find the end of the String. (can a String span several lines?)
        if (this.inString) {

            final int index = string.indexOf("\"");

            if (index >= 0) {
                if ((index == 0) || (string.charAt(index - 1) != '\\')) {
                    result = string.substring(0, index + "\"".length()) + CLOSESTRING;
                    this.inString = false;
                    result += this.decorate(string.substring(index + "\"".length()));
                } else {
                    result = string.substring(0, index + "\"".length());
                    result += this.decorate(string.substring(index + "\"".length()));
                }
            } // else  echo(string); already set 

            return result;
        }
        // find the end of the comment
        if (this.inComment) {

            final int index = string.indexOf("*/");

            if (index >= 0) {
                result = string.substring(0, index + "*/".length()) + CLOSECOMMENT;
                this.inComment = false;
                result += this.decorate(string.substring(index + "*/".length()));
            } //else  echo(string); already done

            return result;
        }

        // OK, not already in tag, comment or string, so look for either of those
        //  this regexp describes the start of tag, comment or string
        final Matcher m = Pattern.compile("(.*?)(<|//|/\\*|\").*").matcher(string);
        //  this regexp describes the start of tag, comment or string
        //Matcher m = Pattern.compile("(<|//|/\\*|\")").matcher(string); 
        // found no tag, string or comment, so just highlight the following java
        if (!m.matches()) {
            // for all the patterns defined, do regexp replacement 

            for (int i = 0; i < this.pattern.length; i++)
                result = this.pattern[i].matcher(result).replaceAll(this.replacement[i]);

            // pattern reuse is faster than String.replaceAll(regexp, replacement)
            return result;
        }
        // skip the first "(.*?), go to the second group instead 
        final int matchStart = m.start(2);
        // get this group's first character.
        final char ch = m.group().charAt(matchStart);

        switch (ch) {
            // This is the start of an xmlTag
            case '<': {
                result = this.decorate(string.substring(0, matchStart));
                result += "<";
                this.inXmlTag = true;
                result += this.decorate(string.substring(matchStart + "<".length()));

                return result;
            }
                // This is the start of a String
            case '"': {
                result = this.decorate(string.substring(0, matchStart));
                result += OPENSTRING + "\"";
                this.inString = true;
                result += this.decorate(string.substring(matchStart + "\"".length()));

                return result;
            }
                // this is the start of a comment
            case '/': {
                // This is the start of a comment like //
                if (string.charAt(matchStart + 1) == '/') {
                    result = this.decorate(string.substring(0, matchStart));
                    result += OPENCOMMENT + string.substring(matchStart) + CLOSECOMMENT;
                }
                // This is the start of a comment like /* 
                else {
                    result = this.decorate(string.substring(0, matchStart));
                    result += OPENCOMMENT + "/*";
                    this.inComment = true;
                    result += this.decorate(string.substring(matchStart + "/*".length()));
                }

                return result;
            }
            default:
                throw new IllegalStateException("Matcher said match was found, but char " +
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
