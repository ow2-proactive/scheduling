package unitTests;

import org.junit.Test;
import org.ow2.proactive.utils.Tools;

import org.junit.Assert;


public class TestTimePeriodParser {

    @Test
    public void test() {
        final long second = 1000;
        final long minute = 1000 * 60;
        final long hour = 1000 * 60 * 60;
        final long day = 1000 * 60 * 60 * 24;
        Assert.assertEquals(second, Tools.parsePeriod("1s"));
        Assert.assertEquals(minute, Tools.parsePeriod("1m"));
        Assert.assertEquals(hour, Tools.parsePeriod("1h"));
        Assert.assertEquals(day, Tools.parsePeriod("1d"));
        Assert.assertEquals(1234 * second, Tools.parsePeriod("1234s"));
        Assert.assertEquals(3 * day + 5 * hour, Tools.parsePeriod("3d 5h"));
        Assert.assertEquals(second + 2 * minute + 3 * hour + 4 * day, Tools.parsePeriod("1s 2m 3h 4d"));
        Assert.assertEquals(33 * hour + 10 * minute, Tools.parsePeriod(" 33h     10m "));

        testFails("");
        testFails("1");
        testFails("1a");
        testFails("s");
        testFails("-1s");
    }

    private void testFails(String periodString) {
        try {
            Tools.parsePeriod(periodString);
            Assert.fail("Parsing for " + periodString + " should fail");
        } catch (IllegalArgumentException e) {
            System.out.println("Expected error: " + e);
        }
    }

}
