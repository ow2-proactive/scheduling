package modelisation.edu.cornell.lassp.houle.RngPack;


//
// RngPack 1.0 by Paul Houle
// http://www.msc.cornell.edu/~houle/rngpack 
//

/**
 *
 *
 * RandomApp is a simple application that demonstrates the use of
 * RngPack.  RandomApp generates random numbers and writes them to the
 * standard output.  This is very useful on Unix systems since the
 * output can be piped to another application.
 * See <A HREF="../RandomApp.txt">RandomApp</A> documentation for
 * how to run it.
 *
 *
 *
 * <P>
 * <A HREF="../src/edu/cornell/lassp/houle/RngPack/RandomApp.java">
 * Source code </A> is available.
 *
 * @author <A HREF="http://www.msc.cornell.edu/~houle"> Paul Houle </A> (E-mail: <A HREF="mailto:houle@msc.cornell.edu">houle@msc.cornell.edu</A>)
 * @version 1.0
 *
 * @see RandomJava
 * @see RandomShuffle
 */
public class RandomApp {
    static String[] generators = {
            "ranmar", "ranecu", "ranlux", "randomjava", "null"
        };
    static String[] distributions = { "flat", "gaussian" };
    static int RANMAR = 0;
    static int RANECU = 1;
    static int RANLUX = 2;
    static int RANJAVA = 3;
    static int NULL = 4;
    static int FLAT = 0;
    static int GAUSSIAN = 1;

    public static void main(String[] args) {
        String a;
        RandomElement e;
        int i;
        int j;
        int generator;
        int distribution;
        int n;
        int luxury;
        double x;
        long seed;
        boolean seeded = false;
        boolean gselected = false;
        boolean dselected = false;
        boolean noprint = false;
        boolean numbered = false;
        boolean luxuryset = false;

        generator = RANMAR;
        distribution = FLAT;
        seed = RandomSeedable.ClockSeed();
        luxury = Ranlux.lxdflt;
        n = 1;


/* Primitive non-chomskyian parser for command line arguments. */
parse_loop: 
        for (i = 0; i < args.length; i++) {
            a = new String(args[i]);
            a.toLowerCase();

            if (a.compareTo("noprint") == 0) {
                noprint = true;
                continue;
            }
            ;

            if (a.compareTo("seed") == 0) {
                if (seeded) {
                    die("RandomApp: only one seed can be passed");
                }
                if (i == (args.length - 1)) {
                    die("RandomApp: missing seed.");
                }

                i++;
                a = new String(args[i]);

                try {
                    seed = Long.parseLong(a);
                } catch (NumberFormatException ex) {
                    die("RandomApp: seed is not a valid number.");
                }
                ;

                seeded = true;
                continue;
            }
            ;

            if (a.compareTo("luxury") == 0) {
                if (luxuryset) {
                    die("RandomApp: only one luxury level can be passed");
                }
                if (i == (args.length - 1)) {
                    die("RandomApp: missing luxury level.");
                }

                i++;
                a = new String(args[i]);

                try {
                    luxury = Integer.parseInt(a);
                } catch (NumberFormatException ex) {
                    die("RandomApp: luxury level is not a valid number.");
                }
                ;

                luxuryset = true;

                if ((luxury < 0) || (luxury > Ranlux.maxlev)) {
                    die("RandomApp: luxury level must be between 0 and " +
                        Ranlux.maxlev);
                }
                continue;
            }
            ;

            for (j = 0; j < generators.length; j++)
                if (a.compareTo(generators[j]) == 0) {
                    if (gselected) {
                        die("RandomApp: only one generator can be selected.");
                    }
                    generator = j;
                    gselected = true;
                    continue parse_loop;
                }
            ;

            for (j = 0; j < distributions.length; j++)
                if (a.compareTo(distributions[j]) == 0) {
                    if (dselected) {
                        die("RandomApp: only one distribution can be selected.");
                    }
                    distribution = j;
                    dselected = true;
                    continue parse_loop;
                }
            ;

            try {
                n = Integer.parseInt(a);
                if (numbered) {
                    die(
                        "RandomApp: only one number of random numbers can be selected.");
                }
                numbered = true;
            } catch (NumberFormatException ex) {
                die("RandomApp: syntax error <" + a + ">");
            }
            ;
        }
        ;

        e = null;

        if (generator == RANMAR) {
            e = new Ranmar(seed);
        } else if (generator == RANECU) {
            e = new Ranecu(seed);
        } else if (generator == RANLUX) {
            e = new Ranlux(luxury, seed);
        } else if (generator == RANJAVA) {
            e = new RandomJava();
        }
        ;

        for (i = 1; i <= n; i++) {
            if (distribution == FLAT) {
                if (generator != NULL) {
                    x = e.raw();
                } else {
                    x = 0.0;
                }
            } else {
                if (generator != NULL) {
                    x = e.gaussian();
                } else {
                    x = 0.0;
                }
            }

            if (!noprint) {
                System.out.println(x);
            }
        }
        ;
    }
    ;
    static void die(String s) {
        System.err.println(s);
        System.exit(-1);
    }
    ;
    static double nullgen() {
        return 0.0;
    }
    ;
}
;
