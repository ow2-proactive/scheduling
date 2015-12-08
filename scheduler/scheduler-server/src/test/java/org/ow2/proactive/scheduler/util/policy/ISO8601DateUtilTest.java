package org.ow2.proactive.scheduler.util.policy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;


public class ISO8601DateUtilTest {

    @Test(expected = IllegalArgumentException.class)
    public void testformatException() throws ParseException {
        ISO8601DateUtil.toDate("2015-12-02T14:zz");
    }

    @Test
    public void testParseNewDateToISO8601() throws ParseException {
        Date expectedDate = new Date(0);
        assertThat(ISO8601DateUtil.toDate(ISO8601DateUtil.parse(expectedDate)).getTime(),
                is(expectedDate.getTime()));
    }

    @Test
    public void testParseToISO8601() throws ParseException {
        DateTime expectedDateTime = new DateTime(2015, 12, 2, 14, 50, 0, 0, DateTimeZone.forOffsetHours(6));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00+06:00").getTime(),
                is(expectedDateTime.toDate().getTime()));
    }

    @Test
    public void testISO8601ComplaintStringPlus2Hours() throws ParseException {
        DateTime expectedDateTime = new DateTime(2015, 12, 2, 14, 50, 0, 0, DateTimeZone.forOffsetHours(2));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00+02:00").getTime(),
                is(expectedDateTime.toDate().getTime()));
    }

    @Test
    public void testISO8601ComplaintStringMinus2Hours() throws ParseException {
        DateTime expectedDateTime = new DateTime(2015, 12, 2, 14, 50, 0, 0, DateTimeZone.forOffsetHours(-2));
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00-02:00").getTime(),
                is(expectedDateTime.toDate().getTime()));
    }

    @Test
    public void testLocalTime() throws ParseException {
        Date expectedDate = getExpectedDate("2015-12-02T14:50:00");
        assertThat(ISO8601DateUtil.toDate("2015-12-02T14:50:00").getTime(), is(expectedDate.getTime()));
    }

    private Date getExpectedDate(String dateString) throws ParseException {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getDefault());
        Date date = isoFormat.parse(dateString);
        return date;
    }

}
