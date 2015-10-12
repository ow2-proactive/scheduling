/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal.webapp;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.resteasy.annotations.StringParameterUnmarshallerBinder;
import org.jboss.resteasy.spi.StringParameterUnmarshaller;
import org.jboss.resteasy.util.FindAnnotation;


/**
 * Will try to parse a date as string in ISO 8601 format or fall back on default Date.toString() format (when
 * using the Java REST API).
 * @see <a href="http://docs.jboss.org/resteasy/2.0.0.GA/userguide/html/StringConverter.html#StringParamUnmarshaller">
 *      From Resteasy documentation</a>
 */
public class DateFormatter implements StringParameterUnmarshaller<Date> {
    /** Format used by Date#toString() */
    private static final String TO_STRING_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    private SimpleDateFormat formatter;
    private SimpleDateFormat toStringFormatter;

    public void setAnnotations(Annotation[] annotations) {
        DateFormat format = FindAnnotation.findAnnotation(annotations, DateFormat.class);
        formatter = new SimpleDateFormat(format.value());
        toStringFormatter = new SimpleDateFormat(TO_STRING_FORMAT);
    }

    public Date fromString(String str) {
        try {
            return formatter.parse(str);
        } catch (Exception iso8601Exception) {
            try {
                return toStringFormatter.parse(str);
            } catch (Exception toStringFormatException) {
                throw new IllegalArgumentException(toStringFormatException);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @StringParameterUnmarshallerBinder(DateFormatter.class)
    public @interface DateFormat {
        String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mmZ";

        String value() default ISO_8601_FORMAT;

    }
}
