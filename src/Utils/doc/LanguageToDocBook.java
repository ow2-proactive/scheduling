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


/** All possible ways of transforming a code file into a decorated
 * code file should implement this interface. */
public interface LanguageToDocBook {
    /** A few tags used in the resulting highlighted docbook */
    static final String SPAN = "<emphasis role=";

    /** A few tags used in the resulting highlighted docbook */
    static final String OPENCOMMENT = SPAN + "\"comment\">";

    /** A few tags used in the resulting highlighted docbook */
    static final String CLOSECOMMENT = "</emphasis>";

    /** A few tags used in the resulting highlighted docbook */
    static final String OPENKEYWORD = SPAN + "\"keyword\">";

    /** A few tags used in the resulting highlighted docbook */
    static final String CLOSEKEY = CLOSECOMMENT;

    /** A few tags used in the resulting highlighted docbook */
    static final String OPENCODE = SPAN + "\"codeword\">";

    /** A few tags used in the resulting highlighted docbook */
    static final String CLOSECODE = CLOSECOMMENT;

    /** A few tags used in the resulting highlighted docbook */
    static final String OPENTYPE = SPAN + "\"typeword\">";

    /** A few tags used in the resulting highlighted docbook */
    static final String CLOSETYPE = CLOSECOMMENT;

    /** A few tags used in the resulting highlighted docbook */
    static final String OPENSTRING = SPAN + "\"string\">";

    /** A few tags used in the resulting highlighted docbook */
    static final String CLOSESTRING = CLOSECOMMENT;

    /** Add docbook tags to the parameter code String.
     * @param codeString the initial String of code, which is to be beautified.
     * @return the initial String, with intersparsed docbook tags <=> decorated code */
    String convert(String codeString);

    /** Once an instance of this Class is created, check that it will really work when run.
     * @return true if running it will do the conversion, false if it will have trouble.
     * For example, trouble may be created by a missing helper program. */
    boolean willWork();
}
