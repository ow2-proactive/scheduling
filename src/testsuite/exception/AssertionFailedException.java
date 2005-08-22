/*
 * Created on Jul 16, 2004
 * author : Matthieu Morel
 */
package testsuite.exception;


/**
 * @author Matthieu Morel
 */
public class AssertionFailedException extends RuntimeException {
    public AssertionFailedException() {
        super();
    }

    public AssertionFailedException(String message) {
        super(message);
    }
}
