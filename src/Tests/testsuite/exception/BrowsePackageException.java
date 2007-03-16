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
package testsuite.exception;


/** When you add automatly tests from a package to a group.
 * @author Alexandre di Costanzo
 */
public class BrowsePackageException extends Exception {

    /** To comstruct a new BrowsePackageException.
     */
    public BrowsePackageException() {
        super("Exception in browsing package to find Tests.");
    }

    /** To comstruct a new BrowsePackageException.
     * @param message a string to describe your exception
     */
    public BrowsePackageException(String message) {
        super(message);
    }

    /** To comstruct a new BrowsePackageException.
     * @param message a string to describe your exception
     * @param cause a cause of a exception
     */
    public BrowsePackageException(String message, Throwable cause) {
        super(message, cause);
    }

    /** To comstruct a new BrowsePackageException.
     * @param cause a cause of a exception
     */
    public BrowsePackageException(Throwable cause) {
        super(cause);
    }
}
