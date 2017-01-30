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
package org.ow2.proactive.scheduler.common.job.factories.spi.model.factory;

import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.IdentityConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ListValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;


public class ListParserValidator extends BaseParserValidator<String> {

    public static final String LIST_TYPE = "LIST";

    public static final String LIST_TYPE_REGEXP = "[Ll][Ii][Ss][Tt]";

    public ListParserValidator(String model) throws ModelSyntaxException {
        super(model);
    }

    @Override
    public String getType() {
        return LIST_TYPE;
    }

    @Override
    public String getTypeRegexp() {
        return LIST_TYPE_REGEXP;
    }

    @Override
    public Class getClassType() {
        return String.class;
    }

    @Override
    protected Converter<String> createConverter(String model) throws ModelSyntaxException {
        return new IdentityConverter();
    }

    @Override
    protected Validator<String> createValidator(String model, Converter<String> converter) throws ModelSyntaxException {
        String listRegexp = "^" + LIST_TYPE_REGEXP + "\\(([^)]+)\\)$";

        String commaSeparatedValuesString = parseAndGetOneGroup(model, listRegexp);

        String[] stringValues = commaSeparatedValuesString.split(",");
        ImmutableList.Builder<String> listBuilder = new ImmutableList.Builder<>();
        if (ImmutableSet.copyOf(stringValues).size() < stringValues.length) {
            throw new ModelSyntaxException("Duplicate value in LIST");
        }
        for (String value : stringValues) {
            String trimmedValue = value.trim();
            listBuilder.add(trimmedValue);
        }
        return new ListValidator(listBuilder.build());
    }

}
