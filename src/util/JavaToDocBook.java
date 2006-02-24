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

import java.io.IOException;


/** All possible ways of transforming a code file into a decorated code file
 * should implement this interface. */
public interface JavaToDocBook {
    /** Convert a code file into a decorated code file.
     * @param fileToConvert the name of the file to convert
     * @return convertedFile : the name of the file which has been created (it contains decorated code)  */
    String convert(String fileToConvert) throws IOException;

    /** Once an instance of this Class is created, check that it will really work when run.
     * @return true if running it will do the conversion, false if it will have trouble;
     * For example, trouble may be created by a missing extension. */
    boolean willWork();
}
