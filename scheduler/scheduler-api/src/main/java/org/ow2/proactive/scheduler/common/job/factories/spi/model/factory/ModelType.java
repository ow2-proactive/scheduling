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
import java.net.URL;
import java.util.Date;


/**
 * @author ActiveEon Team
 * @since 19/08/19
 */
public enum ModelType {
    BOOLEAN(BooleanParserValidator.class, Boolean.class),
    CATALOG_OBJECT(CatalogObjectParserValidator.class, String.class),
    CRON(CRONParserValidator.class, String.class),
    DATETIME(DateTimeParserValidator.class, Date.class),
    DOUBLE(DoubleParserValidator.class, Double.class),
    FLOAT(FloatParserValidator.class, Float.class),
    INTEGER(IntegerParserValidator.class, Integer.class),
    JSON(JSONParserValidator.class, String.class),
    LIST(ListParserValidator.class, String.class),
    LONG(LongParserValidator.class, Long.class),
    MODEL_FROM_URL(ModelFromURLParserValidator.class, String.class),
    NOT_EMPTY_STRING(NotEmptyParserValidator.class, String.class),
    REGEXP(RegexpParserValidator.class, String.class),
    SHORT(ShortParserValidator.class, Short.class),
    SPEL(SPELParserValidator.class, String.class),
    URI(URIParserValidator.class, URI.class),
    URL(URLParserValidator.class, URL.class),
    HIDDEN(HiddenParserValidator.class, String.class),
    CREDENTIAL(CredentialParserValidator.class, String.class);

    // The parser validator of the model type
    private Class typeParserValidator;

    // The parameter string value is expected to be converted to which class by its parser
    private Class classType;

    ModelType(Class typeParserValidator, Class classType) {
        this.typeParserValidator = typeParserValidator;
        this.classType = classType;
    }

    public Class getTypeParserValidator() {
        return typeParserValidator;
    }

    public Class getClassType() {
        return classType;
    }
}
