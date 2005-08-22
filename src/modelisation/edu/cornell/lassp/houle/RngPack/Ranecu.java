package modelisation.edu.cornell.lassp.houle.RngPack;

import java.util.*;


//
// RngPack 1.0 by Paul Houle
// http://www.msc.cornell.edu/~houle/rngpack 
//

/**
 *
 * Ranecu is an advanced multiplicative linear congruential random number
 * generator with a period of aproximately 10<SUP>18</SUP>.
 * Ranecu is a direct translation from Fortran of the <B>RANECU</B>
 * subroutine
 * published in the paper
 * <BR>
 * F. James, <CITE>Comp. Phys. Comm.</CITE> <STRONG>60</STRONG> (1990) p 329-344
 * <BR>
 * The algorithm was originally described in
 * <BR>
 * P. L'Ecuyer, <CITE>Commun. ACM.</CITE> <STRONG>1988</STRONG> (1988) p 742
 * <BR>
 *
 * <P>
 * <A HREF="../src/edu/cornell/lassp/houle/RngPack/Ranecu.java" TARGET="edu.cornell.lassp.houle.source">
 * Source code </A> is available.
 *
 * @author <A HREF="http://www.msc.cornell.edu/~houle"> Paul Houle </A> (E-mail: <A HREF="mailto:houle@msc.cornell.edu">houle@msc.cornell.edu</A>)
 * @version 1.0
 */
public class Ranecu extends RandomSeedable {
    int iseed1;
    int iseed2;

    /**
     * default iseed1 = 12345
     */
    public static int DEFSEED1 = 12345;

    /**
     * default iseed2 = 67890
     */
    public static int DEFSEED2 = 67890;

    /**
     *
     * Initialize <BOLD>RANECU</BOLD> with the default seeds from
     * James.
     *
     */
    public Ranecu() {
        iseed1 = DEFSEED1;
        iseed2 = DEFSEED2;
    }
    ;

    /**
     *
     * Initialize <BOLD>RANECU</BOLD> with two specified integer seeds.  Use
     * this to introduce repeatable seeds.  Equivalent to
     *
     * <CODE>Ranecu(s1*(long) Integer.MAX_VALUE)+s2)</CODE>
     *
     * @param s1 seed integer 1 (MSW)
     * @param s2 seed integer 2 (LSW)
     *
     */
    public Ranecu(int s1, int s2) {
        iseed1 = s1;
        iseed2 = s2;
    }
    ;

    /*
     *
     * Initialize <TT>RANECU</TT> with a long seed.
     *
     * @param l long integer seed
     */
    public Ranecu(long l) {
        iseed1 = (int) l / Integer.MAX_VALUE;
        iseed2 = (int) l % Integer.MAX_VALUE;
    }
    ;

    /*
     *
     * Initialize <TT>RANECU</TT> from the clock without saving a copy
     * of the seed.  Example:
     *
     * <PRE>
     * RandomElement e = new Ranecu(new Date());
     * </PRE>
     *
     * to save the seed for future restarts,  see the <CODE>ClockSeed()</CODE>
     * method defined in RandomSeedable.
     *
     * @param d a date,  typically <CODE>new Date()</CODE>
     * @see RandomSeedable#ClockSeed()
     */
    public Ranecu(Date d) {
        iseed1 = (int) d.getTime() / Integer.MAX_VALUE;
        iseed2 = (int) d.getTime() % Integer.MAX_VALUE;
    }
    ;

    /**
       @see RandomElement#raw
     */
    final public double raw() {
        int k;
        int iz;

        k = iseed1 / 53688;
        iseed1 = (40014 * (iseed1 - (k * 53668))) - (k * 12211);
        if (iseed1 < 0) {
            iseed1 = iseed1 + 2147483563;
        }

        k = iseed2 / 52774;
        iseed2 = (40692 * (iseed2 - (k * 52774))) - (k * 3791);
        if (iseed2 < 0) {
            iseed2 = iseed2 + 2147483399;
        }

        iz = iseed1 - iseed2;
        if (iz < 1) {
            iz = iz + 2147483562;
        }

        return (iz * 4.656613e-10);
    }
    ;

    /**
     * This is an inline version that returns an array of doubles for speed.
     */
    final public void raw(double[] d, int n) {
        int i;
        int k;
        int iz;

        for (i = 0; i < n; i++) {
            k = iseed1 / 53688;
            iseed1 = (40014 * (iseed1 - (k * 53668))) - (k * 12211);
            if (iseed1 < 0) {
                iseed1 = iseed1 + 2147483563;
            }

            k = iseed2 / 52774;
            iseed2 = (40692 * (iseed2 - (k * 52774))) - (k * 3791);
            if (iseed2 < 0) {
                iseed2 = iseed2 + 2147483399;
            }

            iz = iseed1 - iseed2;
            if (iz < 1) {
                iz = iz + 2147483562;
            }

            d[i] = iz * 4.656613e-10;
        }
        ;
    }
    ;

    /**
     *
     * @return the current generator state as a long.  Can be used to
     * restart the generator where one left off.
     *
     */
    public long getSeed() {
        return (iseed1 * (long) Integer.MAX_VALUE) + iseed2;
    }
    ;
}
;
