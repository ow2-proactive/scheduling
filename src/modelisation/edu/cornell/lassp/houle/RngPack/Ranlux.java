package modelisation.edu.cornell.lassp.houle.RngPack;

import java.util.*;


//
// RngPack 1.0 by Paul Houle
// http://www.msc.cornell.edu/~houle/rngpack
//

/**
 *
 *     <TT>RANLUX</TT> is an advanced pseudo-random number generator based on the
 *     <TT>RCARRY</TT> algorithm proposed in 1991 by Marsaglia and Zaman.
 *     <TT>RCARRY</TT>
 *     used a subtract-and-borrow algorithm with a period on the order of
 *     10<SUP>171</SUP> but still had detectable correlations between
 *     numbers.  Martin Luescher proposed the <TT>RANLUX</TT>
 *     algorithm in 1993;  <TT>RANLUX</TT> generates pseudo-random numbers using
 *     <TT>RCARRY</TT> but throws away numbers to destroy correlations.
 *     Thus RANLUX trades execution speed for quality;  by choosing a
 *     larger luxury setting one gets better random numbers slower.
 *     By the tests availible at the time it was proposed,
 *     <TT>RANLUX</TT> at the default luxury setting appears to be a
 *     significant advance quality over previous
 *     generators.
 *
 * <BR>
 * <BR>
 * <CENTER>
 * <TABLE BORDER WIDTH=80%>
 * <TR>
 * <TD ALIGN=center COLSPAN=3>
 * <A NAME="luxury"><FONT SIZE=+2>LUXURY LEVELS</FONT></A>
 * </TD>
 * </TR>
 * <TR>
 * <TD>level</TD><TD ALIGN=center>p</TD><TD><BR></TD>
 * </TR>
 * <TR><TD ALIGN=center>0</TD> <TD ALIGN="center">24</TD>
 * <TD>equivalent to the original <TT>RCARRY</TT> of Marsaglia
 * and Zaman, very long period, but fails many tests. </TD></TR>
 * <TR>
 * <TD ALIGN=center>1</TD><TD ALIGN=center>48</TD><TD>considerable improvement in quality over level 0,
 * now passes the gap test, but still fails spectral test.</TD></TR>
 * <TR>
 * <TD ALIGN=center>2</TD><TD ALIGN=center>97</TD><TD> passes all known tests, but theoretically still
 * defective.</TD></TR><TR BGCOLOR="#FFA0A0">
 * <TD ALIGN=center BGCOLOR="#FFA0A0">3</TD><TD ALIGN=center>223</TD><TD>
 * DEFAULT VALUE.  Any theoretically possible
 * correlations have very small chance of being observed.</TD></TR><TR>
 * <TD ALIGN=center>4<TD ALIGN=center>389</TD><TD>highest possible luxury, all 24 bits chaotic.</TD></TR>
 *
 * </TABLE>
 * </CENTER>
 * <BR>
 * <CENTER><FONT SIZE=+1>
 * <B>VALIDATION</B></FONT>
 * </CENTER>
 *
 * The Java version of <TT>RANLUX</TT> has been verified against published
 * values of numbers 1-5 and 101-105 produced by the reference implementation
 * of <TT>RANLUX</TT> for the following initial conditions:
 *
 * <UL>
 * <LI> Default initialization:  <CODE>Ranlux()</CODE>
 * <LI> Initialization with:     <CODE>Ranlux(0,0)</CODE>
 * <LI> Initialization with:     <CODE>Ranlux(389,1)</CODE>
 * <LI> Initialization with:     <CODE>Ranlux(75,0)</CODE>
 * </UL>
 *  References:
 * <UL>
 * <LI>
 *  M. Luscher, <CITE> Computer Physics Communications</CITE>  <B>79</B> (1994) 100
 * <LI>
 *  F. James, <CITE>Computer Physics Communications</CITE> <B>79</B> (1994) 111
 * <LI><A HREF="http://www.mpa-garching.mpg.de/~tomek/htmls/refs/ranlux.about.html">About <TT>RANLUX</TT> random number generator:  Excerpts from discussion in the Usenet news groups</A>
 * <LI><A HREF="http://www.mpa-garching.mpg.de/~tomek/htmls/refs/ranlux.f90_2.html">Miller's FORTRAN 90 implementation of <TT>RANLUX</TT> with test code</A>
 * </UL>
 *
 *
 * <P>
 * <A HREF="../src/edu/cornell/lassp/houle/RngPack/Ranlux.java" TARGET="edu.cornell.lassp.houle.source">
 * Source code </A> is available.
 *
 * @author <A HREF="http://www.msc.cornell.edu/~houle" TARGET="edu.cornell.lassp.houle.author"> Paul Houle </A> (E-mail: <A HREF="mailto:houle@msc.cornell.edu">houle@msc.cornell.edu</A>)
 * @version 1.0
 */
public class Ranlux extends RandomSeedable {

    /**
     * Maximum luxury level: <CODE>maxlev=4</CODE>
     */
    public static final int maxlev = 4;

    /**
     * Default luxury level:  <CODE>lxdflt=3</CODE>
     */
    public static final int lxdflt = 3;
    static final int igiga = 1000000000;
    static final int jsdflt = 314159265;
    static final int twop12 = 4096;
    static final int itwo24 = 1 << 24;
    static final int icons = 2147483563;
    static final int[] ndskip = { 0, 24, 73, 199, 365 };
    int[] iseeds;
    int[] isdext;
    int[] next;
    int luxlev = lxdflt;
    int nskip;
    int inseed;
    int jseed;
    int in24 = 0;
    int kount = 0;
    int mkount = 0;
    int i24 = 24;
    int j24 = 10;
    float[] seeds;
    float carry = (float) 0.0;
    float twom24;
    float twom12;
    boolean diagOn = false;

    /**
     * Default initialization of <TT>RANLUX</TT>.  Uses default seed
     * <CODE>jsdflt=314159265</CODE> and luxury level 3.
     */
    public Ranlux() {
        init_arrays();
        rluxdef();
    }

    /**
     * Initialize <TT>RANLUX</TT> with specified <A HREF="#luxury">luxury level</A>
     * and seed.
     *
     * @param lux <A HREF="#luxury">luxury level</A> from 0-4.
     * @param ins seed,  a positive integer.
     *
     */
    public Ranlux(int lux, int ins) {
        init_arrays();
        rluxgo(lux, Math.abs(ins));
    }

    /**
     * Initialize <TT>RANLUX</TT> with specified <A HREF="#luxury">luxury level</A>
     * and seed.
     *
     * @param lux <A HREF="#luxury">luxury level</A> from 0-4.
     * @param ins seed,  a positive long.
     *
     */
    public Ranlux(int lux, long ins) {
        init_arrays();
        rluxgo(lux, Math.abs((int) (ins % Integer.MAX_VALUE)));
    }

    /**
     *
     * Initialize <TT>RANLUX</TT> with default <A HREF="#luxury">luxury level</A>
     * and a specified seed.
     *
     * @param ins seed,  a positive integer
     */
    public Ranlux(int ins) {
        init_arrays();
        rluxgo(lxdflt, Math.abs(ins));
    }

    /**
     *
     * Initialize <TT>RANLUX</TT> with default <A HREF="#luxury">luxury level</A>
     * and a specified seed.
     *
     * @param ins seed,  a positive integer
     */
    public Ranlux(long ins) {
        init_arrays();
        rluxgo(lxdflt, Math.abs((int) (ins % Integer.MAX_VALUE)));
    }

    public void setSeed(long seed) {
        //rluxgo(lxdflt,Math.abs((int) (seed%Integer.MAX_VALUE)));
    }

    /**
     *
     * Initialize <TT>RANLUX</TT> with specified <A HREF="#luxury">luxury level</A>
     * and a Date object.  Can be used to conveniently initialize <TT>RANLUX</TT>
     * from the clock,
     *
     * <PRE>
     * RandomElement e = Ranlux(4,new Date());
     * </PRE>
     *
     * @param lux <A HREF="#luxury">luxury</A> level from 0-4.
     * @param d date used to generate seed
     *
     */
    public Ranlux(int lux, Date d) {
        init_arrays();
        rluxgo(lux, (int) (ClockSeed(d) % Integer.MAX_VALUE));
    }

    /**
     *
     * Initialize <TT>RANLUX</TT> with default <A HREF="#luxury">luxury level</A>
     * and a Date object.  Can be used to conveniently initialize <TT>RANLUX</TT>
     * from the clock,
     *
     * <PRE>
     * RandomElement e = Ranlux(new Date());
     * </PRE>
     *
     * @param d date used to generate seed
     *
     */
    public Ranlux(Date d) {
        init_arrays();
        rluxgo(lxdflt, (int) (ClockSeed(d) % Integer.MAX_VALUE));
    }

    /**
     * Turns diagnostic messages on and off.  If <TT>setDiag(true)</TT> is
     * called,  <TT>RANLUX</TT> will print diagnostic information to
     * <TT>System.err</TT>
     *
     * @param b diagnostic message status
     */
    public void setDiag(boolean b) {
        diagOn = b;
    }

    /**
     *
     * The random number generator.
     *
     * @returns a pseudo-random double in the range (0,1)
     */
    public final double raw() {
        int i;
        int k;
        int lp;
        float uni;
        float out;
        uni = seeds[j24] - seeds[i24] - carry;
        if (uni < (float) 0.0) {
            uni = uni + (float) 1.0;
            carry = twom24;
        } else {
            carry = (float) 0.0;
        }
        seeds[i24] = uni;
        i24 = next[i24];
        j24 = next[j24];
        out = uni;
        if (uni < twom12) {
            out += (twom24 * seeds[j24]);
        }

        /* zero is forbidden in case user wants logarithms */
        if (out == 0.0) {
            out = twom24 * twom24;
        }
        in24++;
        if (in24 == 24) {
            in24 = 0;
            kount += nskip;
            for (i = 1; i <= nskip; i++) {
                uni = seeds[j24] - seeds[i24] - carry;
                if (uni < (float) 0.0) {
                    uni = uni + (float) 1.0;
                    carry = twom24;
                } else {
                    carry = (float) 0.0;
                }
                seeds[i24] = uni;
                i24 = next[i24];
                j24 = next[j24];
            }
        }
        kount++;
        if (kount >= igiga) {
            mkount++;
            kount -= igiga;
        }
        return out;
    }

    private void init_arrays() {

        /*
         *
         * converted from fortran:  fortran arrays start at 1,  java arrays start at
         * 0.  Here we take the low road to compatibility...  We declare arrays that
         * are one bigger than the fortran code.  This wastes three ints and a double;
         * If you're porting this to a Commodore 64 you might need the space.
         *
         */
        iseeds = new int[24 + 1];
        isdext = new int[25 + 1];
        next = new int[24 + 1];
        seeds = new float[24 + 1];
    }

    private void rluxdef() {
        int lp;
        int i;
        int k;
        jseed = jsdflt;
        inseed = jseed;
        diag("RANLUX DEFAULT INITIALIZATION: " + jseed);
        luxlev = lxdflt;
        nskip = ndskip[luxlev];
        lp = nskip + 24;
        in24 = 0;
        kount = 0;
        mkount = 0;
        diag("RANLUX DEFAULT LUXURY LEVEL =  " + luxlev + "    p = " + lp);
        twom24 = (float) 1.0;
        for (i = 1; i <= 24; i++) {
            twom24 = twom24 * (float) 0.5;
            k = jseed / 53668;
            jseed = (40014 * (jseed - (k * 53668))) - (k * 12211);
            if (jseed < 0) {
                jseed = jseed + icons;
            }
            iseeds[i] = jseed % itwo24;
        }
        twom12 = twom24 * (float) 4096.0;
        for (i = 1; i <= 24; i++) {
            seeds[i] = iseeds[i] * twom24;
            next[i] = i - 1;
        }
        next[1] = 24;
        i24 = 24;
        j24 = 10;
        carry = (float) 0.0;
        if (seeds[24] == 0.0) {
            carry = twom24;
        }
    }

    private final void rluxgo(int lux, int ins) {
        int ilx;
        int i;
        int iouter;
        int isk;
        int k;
        int inner;
        int izip;
        int izip2;
        float uni;
        if (lux < 00) {
            luxlev = lxdflt;
        } else if (lux <= maxlev) {
            luxlev = lux;
        } else if ((lux < 24) || (lux > 2000)) {
            luxlev = maxlev;
            diag("RANLUX ILLEGAL LUXURY RLUXGO: " + lux);
        } else {
            luxlev = lux;
            for (ilx = 0; ilx <= maxlev; ilx++)
                if (lux == (ndskip[ilx] + 24)) {
                    luxlev = ilx;
                }
        }
        if (luxlev <= maxlev) {
            nskip = ndskip[luxlev];
            diag("RANLUX LUXURY LEVEL SET BY RLUXGO : " + luxlev + " P= " +
                (nskip + 24));
        } else {
            nskip = luxlev - 24;
            diag("RANLUX P-VALUE SET BY RLUXGO TO: " + luxlev);
        }
        in24 = 0;
        if (ins < 0) {
            diag("Illegal initialization by RLUXGO, negative input seed");
        }
        if (ins > 0) {
            jseed = ins;
            diag("RANLUX INITIALIZED BY RLUXGO FROM SEED " + jseed);
        } else {
            jseed = jsdflt;
            diag("RANLUX INITIALIZED BY RLUXGO FROM DEFAULT SEED");
        }
        inseed = jseed;
        twom24 = (float) 1.0;
        for (i = 1; i <= 24; i++) {
            twom24 = twom24 * (float) 0.5;
            k = jseed / 53668;
            jseed = (40014 * (jseed - (k * 53668))) - (k * 12211);
            if (jseed < 0) {
                jseed = jseed + icons;
            }
            iseeds[i] = jseed % itwo24;
        }
        twom12 = twom24 * 4096;
        for (i = 1; i <= 24; i++) {
            seeds[i] = iseeds[i] * twom24;
            next[i] = i - 1;
        }
        next[1] = 24;
        i24 = 24;
        j24 = 10;
        carry = (float) 0.0;
        if (seeds[24] == 0.0) {
            carry = twom24;
        }
        kount = 0;
        mkount = 0;
    }

    private void diag(String s) {
        if (diagOn) {
            System.err.println(s);
        }
    }
}
