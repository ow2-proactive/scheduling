package modelisation.edu.cornell.lassp.houle.RngPack;

//
// RngPack 1.0 by Paul Houle
// http://www.msc.cornell.edu/~houle/rngpack 
//

/**
* RandomShuffle uses one random number generator to shuffle the numbers
* produced by another to obliterate sequential correlations.
* To initialize a RandomShuffle,  pass it two RandomElements.  The
* first RandomElement is used to generate a table of random numbers
* and the second is used to choose one from the table.  An example of
* usage is,
*
* <PRE>
* RandomElement markov=new RandomShuffle(new Ranecu(),new Ranmar(),32)
* </PRE>
*
* which would generate a deck of 32 numbers from <TT>RANECU</TT> and
* use <TT>RANMAR</TT> to choose from the deck.
*
* <BR>
* <B>References:</B>
* <UL>
* <LI> F. James; <CITE>Comp. Phys. Comm.</CITE> <STRONG>60</STRONG> (1990) p 329-344
* <LI> D. Knuth; <CITE>The Art of Computer Programming</CITE> vol. 2, sec 3.2.2
* </UL>
*
* <P>
* <A HREF="../src/edu/cornell/lassp/houle/RngPack/RandomShuffle.java" TARGET="edu.cornell.lassp.houle.source">
* Source code </A> is available.
*
* @author <A HREF="http://www.msc.cornell.edu/~houle" TARGET="edu.cornell.lassp.houle.author"> Paul Houle </A> (E-mail: <A HREF="mailto:houle@msc.cornell.edu">houle@msc.cornell.edu</A>)
* @version 1.0
*/

public class RandomShuffle extends RandomElement {

  RandomElement generatorA,generatorB;
  int decksize;
  double deck[]; 

/**
* @param ga generator to fill shuffle deck
* @param gb geberator to choose from shuffle deck
* @param ds the size of the shuffle deck
*/

  public RandomShuffle(RandomElement ga,RandomElement gb,int ds) {

	generatorA=ga;
        generatorB=gb;
	decksize=ds;

	stackdeck();

};

/**
* The generator.
*
* @see RandomElement#raw
*/

public double raw() {
        double random;
	int i;

	i=generatorB.choose(0,decksize-1);
        random=deck[i];
        deck[i]=generatorA.raw();

	return random;
};

private void stackdeck() {

        int i;
	deck = new double[decksize];
	
	for(i=0;i<decksize;i++)
		deck[i]=generatorA.raw();
};

};


