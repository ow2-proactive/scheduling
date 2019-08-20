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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.IdentityConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;

import com.google.common.base.Strings;


public class ModelFromURLParserValidator extends BaseParserValidator<String> {

    public static final String LEFT_DELIMITER = "(";

    public static final String RIGHT_DELIMITER = ")";

    protected static final String MODEL_FROM_URL_REGEXP = "^" + ignoreCaseRegexp(ModelType.MODEL_FROM_URL.name()) +
                                                          "\\" + LEFT_DELIMITER + "(.+)" + "\\" + RIGHT_DELIMITER + "$";

    public ModelFromURLParserValidator(String model) throws ModelSyntaxException {
        super(model, ModelType.MODEL_FROM_URL, MODEL_FROM_URL_REGEXP);
    }

    private String findFirstNonEmptyLineTrimmed(List<String> lines) {
        String firstNonEmptyLine = null;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                firstNonEmptyLine = line.trim();
                break;
            }
        }
        return firstNonEmptyLine;
    }

    @Override
    protected Converter<String> createConverter(String model) throws ModelSyntaxException {
        return new IdentityConverter();
    }

    @Override
    protected Validator<String> createValidator(String model, Converter<String> converter) throws ModelSyntaxException {
        String urlString = parseAndGetOneGroup(model, MODEL_FROM_URL_REGEXP);

        URL url = null;
        try {
            url = new URL(urlString);
            List<String> lines = IOUtils.readLines(url.openStream(), Charset.defaultCharset());

            String modelReceivedFromURL = null;
            modelReceivedFromURL = findFirstNonEmptyLineTrimmed(lines);

            if (Strings.isNullOrEmpty(modelReceivedFromURL)) {
                throw new ModelSyntaxException("In " + type + " expression, model received from defined url '" +
                                               urlString + "' is empty.");
            }
            if (modelReceivedFromURL.startsWith(type.name())) {
                throw new ModelSyntaxException("In " + type + " expression, model received from defined url '" +
                                               urlString + "' is recursive.");
            }
            return new ModelValidator(modelReceivedFromURL);

        } catch (MalformedURLException e) {
            throw new ModelSyntaxException("In " + type + " expression, defined url '" + urlString + "' is invalid: " +
                                           e.getMessage(), e);
        } catch (IOException e) {
            throw new ModelSyntaxException("In " + type + " expression, defined url '" + urlString +
                                           "' could not be reached: " + e.getMessage(), e);
        }
    }

}
