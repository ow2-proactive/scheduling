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
package org.ow2.proactive.scheduler.common.job.factories.spi.model.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;



/**
 * @author ActiveEon Team
 * @since 06/03/2019
 */
public class JSONValidator implements Validator<String> {

    public JSONValidator() {
        /**
         * ProActive Empty constructor.
         */
    }

    @Override
    public String validate(String parameterValue, ModelValidatorContext context) throws ValidationException {

        if (!isValidJSON(parameterValue)) {
            throw new ValidationException("Expected value should match JSON format, received " + parameterValue);
        }
        return parameterValue;
    }

    public boolean isValidJSON(final String json) {
        boolean valid = false;
        try {
            final JsonParser parser = new ObjectMapper().getJsonFactory()
                    .createJsonParser(json);
            while (parser.nextToken() != null) {
            }
            valid = true;
        } catch (JsonParseException jpe) {
            throw new RuntimeException("Validator error for JSON type : " + jpe); //jpe.printStackTrace();
        } catch (IOException ioe) {
            throw new RuntimeException("Validator I/O error for JSON type : " + ioe); //ioe.printStackTrace();
        }

        return valid;
    }
}