package modelisation.edu.cornell.lassp.houle.RngPack;

import java.util.*;

//
// RngPack 1.0 by Paul Houle
// http://www.msc.cornell.edu/~houle/rngpack 
//

/**
*
* <CODE>RandomSeedable</CODE> is an abstract class that extends the
* <CODE>RandomElement</CODE> class to include the ability to
* automatically generate a valid <CODE>long</CODE> seed from the clock.
* Thus it provides a consistent interface for seeding interchangable
* generators.  It is reccomended that a <CODE>RandomSeedable</CODE> have
* a constructor that takes a <CODE>long</CODE> for a seed.  For example,
* if you write a generator called <CODE>ReallyRandom</CODE>,  you want
* to be able to do
*
* <PRE>
* long seed=ReallyRandom.ClockSeed();
* RandomSeedable e=new ReallyRandom(seed);
* </PRE>
*
* this makes it convenient to keep a copy of the seed in case you want
* to restart the generator with the same seed some time in the future.
*
* <P>
* If one is going to use a long to generate a smaller seed by taking
* <CODE>Clockseed()</CODE> modulus another number,  we reccomend that
* you use a prime number;   this ensures that the generator would have
* the maximum "period" if it were started at regular issues,  for
* instance,  by a batch job.   See <CODE>Ranmar</CODE> for an
* example.
*
* <P>
* <A HREF="../src/edu/cornell/lassp/houle/RngPack/RandomSeedable.java">
* Source code </A> is available.
* 
* @author <A HREF="http://www.msc.cornell.edu/~houle"> Paul Houle </A> (E-mail: <A HREF="mailto:houle@msc.cornell.edu">houle@msc.cornell.edu</A>)
* @version 1.0
* 
* @see Ranecu
* @see Ranlux
* @see Ranmar
*/

public abstract class RandomSeedable extends RandomElement {

/**
*
* Return a long integer seed given a date
*
* @param d a date
* @return a long integer seed
*
*/
   public static long ClockSeed(Date d)
   {
      return d.getTime();
   };

/**
*
* Return a long integer seed calculated from the date.  Equivalent to
* <CODE>ClockSeed(new Date());
*
* @return a long integer seed
*
*/
 
   public static long ClockSeed()
   {
      return ClockSeed(new Date());
   };
};






