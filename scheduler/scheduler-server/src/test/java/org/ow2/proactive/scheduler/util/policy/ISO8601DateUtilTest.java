package org.ow2.proactive.scheduler.util.policy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;


public class ISO8601DateUtilTest {

    private static final String TEST_TIME_ZONE = "Europe/Paris";

    @Test(expected = IllegalArgumentException.class)
    public void testformatException() throws ParseException {
        ISO8601DateUtil.toDate("2015-12-02T14:zz");
    }

    @Test
    public void testParseNewDateToISO8601() throws ParseException {
        Date expectedDate = new Date(0);
        assertThat(ISO8601DateUtil.parse(expectedDate), is("1970-01-01T01:00:00.000+01:00"));
    }

    @Test
    public void testParseToISO8601() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T22:50:00z", TimeZone.getTimeZone(TEST_TIME_ZONE));
        assertThat(ISO8601DateUtil.parse(expectedDate), is("2015-12-02T22:50:00.000+01:00"));
    }

    @Test
    public void testParseISO8601ToISO8601() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T22:50:00+06:00",
                TimeZone.getTimeZone(TEST_TIME_ZONE));
        assertThat(ISO8601DateUtil.parse(expectedDate), is("2015-12-02T22:50:00.000+01:00"));
    }

    @Test
    public void testParseISO8601ToISO8601MinusHours() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T22:50:00-07:00",
                TimeZone.getTimeZone(TEST_TIME_ZONE));
        assertThat(ISO8601DateUtil.parse(expectedDate), is("2015-12-02T22:50:00.000+01:00"));
    }

    @Test
    public void testISO8601ComplaintStringMinus7Hours() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T22:50:00", TimeZone.getTimeZone(TEST_TIME_ZONE));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00-07:00").getTime(), is(expectedDate.getTime()));
    }

    @Test
    public void testISO8601ComplaintStringPlus2Hours() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T13:50:00", TimeZone.getTimeZone(TEST_TIME_ZONE));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00+02:00").getTime(), is(expectedDate.getTime()));
    }

    @Test
    public void testISO8601ComplaintStringMinus1HourAnd30Minutes() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T18:40:00", TimeZone.getTimeZone(TEST_TIME_ZONE));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00-02:50").getTime(), is(expectedDate.getTime()));
    }

    @Test
    public void testISO8601ComplaintDefault() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T15:50:00", TimeZone.getTimeZone(TEST_TIME_ZONE));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00Z").getTime(), is(expectedDate.getTime()));
    }

    @Test
    public void testLocalTime() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T14:50:00", TimeZone.getDefault());
        System.out.println(ISO8601DateUtil.toDate("2015-12-02T14:50:00"));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00").getTime(), is(expectedDate.getTime()));
    }

    @Test
    public void testParseAndTodate() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T14:50:00", TimeZone.getDefault());
        assertThat(ISO8601DateUtil.toDate(ISO8601DateUtil.parse(expectedDate)).getTime(),
                is(expectedDate.getTime()));
    }

    @Test
    public void testParseAndTodateNewDate() throws ParseException {
        Date expectedDate = new Date(0);
        assertThat(ISO8601DateUtil.toDate(ISO8601DateUtil.parse(expectedDate)).getTime(),
                is(expectedDate.getTime()));
    }

    private Date getExpectedDate(String dateString, TimeZone timeZone) throws ParseException {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(timeZone);
        Date date = isoFormat.parse(dateString);
        return date;
    }

}
