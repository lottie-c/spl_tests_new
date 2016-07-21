/**
 * 
 */
package cz.cuni.mff.spl.scanner;

/**
 * @author jdaniel
 * 
 */
@SuppressWarnings("serial")
public class ScannerException extends Exception {
    public ScannerException() {

    }

    public ScannerException(String message) {
        super(message);
    }

    public ScannerException(Throwable cause) {
        super(cause);
    }

    public ScannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
