//@snippet-start primes_sequential_main
package org.objectweb.proactive.examples.userguide.primes.sequential;

/**
 * This class illustrates a sequential algorithm for primality test.
 * 
 * @author ActiveEon Team
 * 
 */
public class Main {

    public static void main(String[] args) {
        // The default value for the candidate to test (is prime)
        long candidate = 4398042316799l;
        // Parse the number from args if there is some
        if (args.length > 0) {
            try {
                candidate = Long.parseLong(args[0]);
            } catch (NumberFormatException numberException) {
                System.err.println(numberException.getMessage());
                System.err.println("Usage: Main <candidate>");
            }
        }
        // We don't need to check numbers greater than the square-root of the
        // candidate in this algorithm
        long squareRootOfCandidate = (long) Math.ceil(Math.sqrt(candidate));
        // Begin from 2 the first known prime number
        long begin = 2;
        // Until the end of the range
        long end = squareRootOfCandidate;
        // Check the primality
        boolean isPrime = Main.isPrime(candidate, begin, end);
        // Display the result
        System.out.println("\n" + candidate + (isPrime ? " is prime." : " is not prime.") + "\n");
    }

    /**
     * Tests a primality of a specified number in a specified range.
     * 
     * @param candidate
     *            the candidate number to check
     * @param begin
     *            starts check from this value
     * @param end
     *            checks until this value
     * @return <code>true</code> if is prime; <code>false</code> otherwise
     */
    public static Boolean isPrime(long candidate, long begin, long end) {
        for (long divider = begin; divider < end; divider++) {
            if ((candidate % divider) == 0) {
                return false;
            }
        }
        return true;
    }
}
//@snippet-end primes_sequential_main