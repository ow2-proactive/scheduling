/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.job.factories.spi.model.converter;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;


/**
 * Accept the blank string by converting it to null. When the value is not blank, use its specific converter to convert its value.
 * @param <T>
 */
public class NullConverter<T> implements Converter<T> {
    Converter<T> converter;

    public NullConverter(Converter<T> converter) {
        this.converter = converter;
    }

    @Override
    public T convert(String parameterValue) throws ConversionException {
        // When the parameter value is not provided, it's null. Otherwise, use its proper converter
        if (StringUtils.isBlank(parameterValue)) {
            return null;
        } else {
            return converter.convert(parameterValue);
        }
    }
}
