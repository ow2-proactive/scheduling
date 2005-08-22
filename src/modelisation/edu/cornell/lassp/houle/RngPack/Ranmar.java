package modelisation.edu.cornell.lassp.houle.RngPack;

import java.util.*;


//
// RngPack 1.0 by Paul Houle
// http://www.msc.cornell.edu/~houle/rngpack 
//

/**
 *
 * <TT>RANMAR</TT> is a lagged Fibonacci generator proposed by Marsaglia and
 * Zaman and is a good research grade generator.  This version of
 * <TT>RANMAR</TT> is based on the paper by James,  which is a good
 * reference for the properties of <TT>RANMAR</TT> and several other
 * generators.
 *
 * <BR>
 * <B>REFERENCES:</B>
 * <BR>
 * F. James, <CITE>Comp. Phys. Comm.</CITE> <STRONG>60</STRONG> (1990) p 329-344
 * <BR>
 * and was originally described in
 * <BR>
 * G. Marsaglia, A. Zaman and W.-W Tsang, <CITE>Stat. Prob. Lett</CITE> <STRONG>9</STRONG> (1990) p 35.
 *
 *
 * <P>
 * <A HREF="../src/edu/cornell/lassp/houle/RngPack/Ranmar.java" TARGET="edu.cornell.lassp.houle.source">
 * Source code </A> is available.
 *
 * @author <A HREF="http://www.msc.cornell.edu/~houle" TARGET="edu.cornell.lassp.houle.author"> Paul Houle </A> (E-mail: <A HREF="mailto:houle@msc.cornell.edu">houle@msc.cornell.edu</A>)
 * @version 1.0
 */
public class Ranmar extends RandomSeedable {
    double c;
    double cd;
    double cm;
    double[] u;
    double[] uvec;
    int i97;
    int j97;

    /**
     * Default seed.  <CODE>DEFSEED=54217137</CODE>
     */
    public static int DEFSEED = 54217137;

    /**
     * The 46,009,220nd prime number,
     * he largest prime less than 9*10<SUP>8</SUP>.  Used as a modulus
     * because this version of <TT>RANMAR</TT> needs a seed between 0
     * and 9*10<SUP>8</SUP> and <CODE>BIG_PRIME</CODE> isn't commensurate
     * with any regular period.
     * <CODE>BIG_PRIME= 899999963</CODE>
     */
    public static int BIG_PRIME = 899999963;

    /**
     *
     * Initialize Ranmar with a specified integer seed
     *
     * @param ijkl seed integer;  <TT>Ranmar(int ijkl)</TT> takes uses
     * <TT>ijkl</TT> modulus <TT>BIG_PRIME</TT> as a seed for <TT>RANMAR.</TT>
     *
     */
    public Ranmar(int ijkl) {
        ranmarin(Math.abs(ijkl % BIG_PRIME));
    }
    ;

    /**
     *
     * Initialize Ranmar with a specified long seed
     *
     * @param ijkl seed long;  <TT>Ranmar(long ijkl)</TT> takes uses
     * <TT>ijkl</TT> modulus <TT>BIG_PRIME</TT> as a seed for <TT>RANMAR.</TT>
     *
     */
    public Ranmar(long ijkl) {
        ranmarin((int) Math.abs(ijkl % BIG_PRIME));
    }
    ;

    /**
     *
     * Initialize Ranmar with a default seed taken from Marsaglia and
     * Zaman's paper.  Equivalent to <CODE>Ranmar(54217137).</CODE>
     *
     */
    public Ranmar() {
        ranmarin(DEFSEED);
    }
    ;

    /**
     *
     * Seed <TT>RANMAR</TT> from the clock.
     *
     * <PRE>
     * RandomElement e=new Ranmar(new Date());
     * </PRE>
     *
     * @param d a Date object to seed Ranmar with,  typically <CODE>new Date()</CODE>
     *
     */
    public Ranmar(Date d) {
        ranmarin((int) ClockSeed(d) % BIG_PRIME);
    }
    ;

    /**
     *
     * Internal methods:  ranmarin is the initialization code for the
     * generator.
     *
     */
    void ranmarin(int ijkl) {
        int ij;
        int kl;
        int i;
        int ii;
        int j;
        int jj;
        int k;
        int l;
        int m;
        double s;
        double t;

        u = new double[97];
        uvec = new double[97];

        ij = ijkl / 30082;
        kl = ijkl - (30082 * ij);

        i = ((ij / 177) % 177) + 2;
        j = (ij % 177) + 2;
        k = ((kl / 169) % 178) + 1;
        l = kl % 169;
        for (ii = 0; ii < 97; ii++) {
            s = 0.0;
            t = 0.5;
            for (jj = 0; jj < 24; jj++) {
                m = (((i * j) % 179) * k) % 179;
                i = j;
                j = k;
                k = m;
                l = ((53 * l) + 1) % 169;
                if (((l * m) % 64) >= 32) {
                    s += t;
                }
                t *= 0.5;
            }
            u[ii] = s;
        }
        c = 362436.0 / 16777216.0;
        cd = 7654321.0 / 16777216.0;
        cm = 16777213.0 / 16777216.0;
        i97 = 96;
        j97 = 32;
    }
    ;

    /**
     * The generator
     * @return a pseudo random number
     */
    final public double raw() {
        double uni;

        uni = u[i97] - u[j97];
        if (uni < 0.0) {
            uni += 1.0;
        }
        u[i97] = uni;
        if (--i97 < 0) {
            i97 = 96;
        }
        if (--j97 < 0) {
            j97 = 96;
        }
        c -= cd;
        if (c < 0.0) {
            c += cm;
        }
        uni -= c;
        if (uni < 0.0) {
            uni += 1.0;
        }
        return (uni);
    }
    ;

    /**
     *
     * A version of the generator for filling arrays,  inlined for speed
     *
     * @param d an array of doubles to be filled
     * @param n size of the array
     *
     */
    final public void raw(double[] d, int n) {
        double uni;

        for (int i = 0; i < n; i++) {
            uni = u[i97] - u[j97];
            if (uni < 0.0) {
                uni += 1.0;
            }
            u[i97] = uni;
            if (--i97 < 0) {
                i97 = 96;
            }
            if (--j97 < 0) {
                j97 = 96;
            }
            c -= cd;
            if (c < 0.0) {
                c += cm;
            }
            uni -= c;
            if (uni < 0.0) {
                uni += 1.0;
            }
            d[i] = uni;
        }
        ;
    }
    ;
}
;
