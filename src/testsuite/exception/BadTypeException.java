/*
 * Created on Jul 23, 2003
 *
 */
package testsuite.exception;


/** If the type of a result is unknow (or undefined).
 * @author Alexandre di Costanzo
 */
public class BadTypeException extends RuntimeException {

    /** To create a new BadtypeException
     */
    public BadTypeException() {
        super("Your type don't exist in this TestResult");
    }
}
