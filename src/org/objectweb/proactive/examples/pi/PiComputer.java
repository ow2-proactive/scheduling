package org.objectweb.proactive.examples.pi;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class handles a partial computation of PI.
 * 
 * @author Matthieu Morel
 *
 */
public class PiComputer implements PiComp{

    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_EVEN;
    private BigDecimal ZERO = new BigDecimal("0");
    private BigDecimal ONE = new BigDecimal("1");
    private BigDecimal OPPOSITE_ONE = new BigDecimal("-1");
    private BigDecimal TWO = new BigDecimal("2");
    private BigDecimal OPPOSITE_TWO = new BigDecimal("-2");
    private BigDecimal FOUR = new BigDecimal("4");
    private BigDecimal FIVE = new BigDecimal("5");
    private BigDecimal SIX = new BigDecimal("6");
    private BigDecimal EIGHT = new BigDecimal("8");
    private BigDecimal OPPOSITE_EIGHT = new BigDecimal("-8");
    private BigInteger SIXTEEN = new BigInteger("16");

    public PiComputer() {
    }

    public PiComputer(Integer scaleObject) {
        try {
            System.out
                    .println("created PiComputer on host "
                            + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ignored) {
        }
        setScale(scaleObject);
    }
    
    public void setScale(Integer scale) {
        
        ZERO = ZERO.setScale(scale);
        ONE = ONE.setScale(scale);
        OPPOSITE_ONE = OPPOSITE_ONE.setScale(scale);
        TWO = TWO.setScale(scale);
        OPPOSITE_TWO = OPPOSITE_TWO.setScale(scale);
        FOUR = FOUR.setScale(scale);
        FIVE = FIVE.setScale(scale);
        SIX = SIX.setScale(scale);
        EIGHT = EIGHT.setScale(scale);
        OPPOSITE_EIGHT = OPPOSITE_EIGHT.setScale(scale);
    }

    /**
     * 
     * @param interval
     * @return
     */
    public Result compute(Interval interval) {
        try {
            System.out
                    .println("Starting computation for interval ["
                            + interval.getBeginning() + " , "
                            + interval.getEnd() + "] on host : "
                            + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ignored) {
            ignored.printStackTrace();
        }
        long startChrono = System.currentTimeMillis();

        BigDecimal bd = ZERO;

        // BBP formula for the given interval
    
        for (int k = interval.getBeginning().intValue(); k <= interval.getEnd()
                .intValue(); k++) {        	
            bd = bd.add(f(k));
        }

        return new Result(bd, System.currentTimeMillis() - startChrono);
    }

    private BigDecimal f(int k) {
        BigDecimal K = new BigDecimal(k);
        BigDecimal EIGHT_K = EIGHT.multiply(K);
        BigDecimal FIRST = ONE.divide(new BigDecimal(SIXTEEN.pow(k)),
                ROUND_MODE);
        BigDecimal SECOND = FOUR.divide(EIGHT_K.add(ONE), ROUND_MODE);
        BigDecimal THIRD = OPPOSITE_TWO.divide(EIGHT_K.add(FOUR), ROUND_MODE);
        BigDecimal FOURTH = OPPOSITE_ONE.divide(EIGHT_K.add(FIVE), ROUND_MODE);
        BigDecimal FIFTH = OPPOSITE_ONE.divide(EIGHT_K.add(SIX), ROUND_MODE);

        return FIRST.multiply(SECOND.add(THIRD.add(FOURTH.add(FIFTH))));
    }
}