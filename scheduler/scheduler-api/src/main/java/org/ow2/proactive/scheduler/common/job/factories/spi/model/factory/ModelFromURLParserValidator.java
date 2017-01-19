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

    public static final String MODEL_FROM_URL_TYPE = "MODEL_FROM_URL";

    public static final String MODEL_FROM_URL_TYPE_REGEXP = "[Mm][Oo][Dd][Ee][Ll]_[Ff][Rr][Oo][Mm]_[Uu][Rr][Ll]";

    public ModelFromURLParserValidator(String model) throws ModelSyntaxException {
        super(model);
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
    public String getType() {
        return MODEL_FROM_URL_TYPE;
    }

    @Override
    public String getTypeRegexp() {
        return MODEL_FROM_URL_TYPE_REGEXP;
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
        String modelFromUrlRegexp = "^" + MODEL_FROM_URL_TYPE_REGEXP + "\\((.+)\\)$";

        String urlString = parseAndGetOneGroup(model, modelFromUrlRegexp);

        URL url = null;
        try {
            url = new URL(urlString);
            List<String> lines = IOUtils.readLines(url.openStream(), Charset.defaultCharset());

            String modelReceivedFromURL = null;
            modelReceivedFromURL = findFirstNonEmptyLineTrimmed(lines);

            if (Strings.isNullOrEmpty(modelReceivedFromURL)) {
                throw new ModelSyntaxException("In " + getType() + " expression, model received from defined url '" +
                                               urlString + "' is empty.");
            }
            if (modelReceivedFromURL.startsWith(MODEL_FROM_URL_TYPE)) {
                throw new ModelSyntaxException("In " + getType() + " expression, model received from defined url '" +
                                               urlString + "' is recursive.");
            }
            return new ModelValidator(modelReceivedFromURL);

        } catch (MalformedURLException e) {
            throw new ModelSyntaxException("In " + getType() + " expression, defined url '" + urlString +
                                           "' is invalid: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ModelSyntaxException("In " + getType() + " expression, defined url '" + urlString +
                                           "' could not be reached: " + e.getMessage(), e);
        }
    }

}
