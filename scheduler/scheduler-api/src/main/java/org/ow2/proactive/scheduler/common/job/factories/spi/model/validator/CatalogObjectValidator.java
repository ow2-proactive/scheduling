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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.ow2.proactive.http.CommonHttpResourceDownloader;
import org.ow2.proactive.scheduler.common.exception.InternalException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * @author ActiveEon Team
 * @since 11/10/2018
 */
public class CatalogObjectValidator implements Validator<String> {
    private static final Logger logger = Logger.getLogger(CatalogObjectValidator.class);

    //catalogObjectModelRegexp = bucketName/objectName[/revision]. The revision is a h-code number represented by 13 digit.

    public static final String CATALOG_OBJECT_MODEL_REGEXP = "^[^/]+/[^/]+(/[^/][0-9]{12})?$";

    private static final String CATALOG_URL_WITH_REVISION = PASchedulerProperties.CATALOG_REST_URL +
                                                            "/buckets/%s/resources/%s/revisions/%s";

    private static final String CATALOG_URL_WITHOUT_REVISION = PASchedulerProperties.CATALOG_REST_URL +
                                                               "/buckets/%s/resources/%s";

    public CatalogObjectValidator() {
        /**
         * ProActive Empty constructor.
         */
    }

    @Override
    public String validate(String parameterValue, ModelValidatorContext context) throws ValidationException {
        Pattern pattern = Pattern.compile(CATALOG_OBJECT_MODEL_REGEXP);
        Matcher matcher = pattern.matcher(parameterValue);
        if (!(parameterValue.matches(CATALOG_OBJECT_MODEL_REGEXP) && matcher.find())) {
            throw new ValidationException("Expected value should match regular expression " + pattern.pattern() +
                                          " , received " + parameterValue);
        }
        if (context == null || context.getSessionId() == null || context.getSessionId().isEmpty()) {
            // Sometimes the workflow is parsed and checked without scheduler instance (e.g., submitted from catalog).
            // In this case, we don't have the access of the scheduler global dataspace, so the validity check is passed.
            logger.debug(String.format("Validity of the variable value [%s] is skipped because access to the catalog is disabled.",
                                       parameterValue));
            return parameterValue;
        }
        try {
            if (!exist(parameterValue, context.getSessionId())) {
                throw new ValidationException(String.format("Catalog object [%s] does not exist or cannot be accessed.",
                                                            parameterValue));
            }
        } catch (PermissionException e) {
            throw new ValidationException(String.format("Access to catalog object [%s] is not authorized.",
                                                        parameterValue));
        } catch (InternalException | IOException e) {
            logger.warn(String.format("Cannot check the validity of the variable value [%s]: Internal error.",
                                      parameterValue),
                        e);
            throw new ValidationException(String.format("Internal error when accessing object [%s], please check the server logs.",
                                                        parameterValue));
        }
        return parameterValue;
    }

    private boolean exist(String catalogObjectValue, String sessionId)
            throws PermissionException, IOException, ValidationException {
        String[] splitCatalog = catalogObjectValue.split("/");
        String url;
        // catalogObjectValue is already checked (with the regular expression) to have either 2 to 3 parts (bucketName/objectName[/revision]) after split
        if (splitCatalog.length == 2) {
            url = String.format(CATALOG_URL_WITHOUT_REVISION, splitCatalog[0], splitCatalog[1]);
        } else if (splitCatalog.length == 3) {
            url = String.format(CATALOG_URL_WITH_REVISION, splitCatalog[0], splitCatalog[1], splitCatalog[2]);
        } else {
            throw new ValidationException("Expected value should match the format: bucketName/objectName[/revision]");
        }
        int catalogObjResponseCode = CommonHttpResourceDownloader.getInstance().getResponseCode(sessionId, url, true);
        switch (catalogObjResponseCode) {
            case HttpStatus.SC_OK:
                return true;
            case HttpStatus.SC_NOT_FOUND:
                return false;
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_FORBIDDEN:
                throw new PermissionException("Permission denied to access the catalog object.");
            default:
                throw new InternalException("Failed to request the catalog object.");
        }
    }
}
