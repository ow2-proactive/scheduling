/*
 * Created on Jul 24, 2003
 *
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
