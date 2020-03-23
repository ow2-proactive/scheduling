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

import java.net.URI;

import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.URIConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.GlobalFileValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;


/**
 * @author ActiveEon Team
 * @since 27/08/19
 */
public class GlobalFileParserValidator extends BaseParserValidator<URI> {

    public GlobalFileParserValidator(String model) throws ModelSyntaxException {
        super(model, ModelType.GLOBAL_FILE);
    }

    @Override
    protected Converter<URI> createConverter(String model) throws ModelSyntaxException {
        return new URIConverter();
    }

    @Override
    protected Validator<URI> createValidator(String model, Converter<URI> converter) throws ModelSyntaxException {
        return new GlobalFileValidator();
    }

}
