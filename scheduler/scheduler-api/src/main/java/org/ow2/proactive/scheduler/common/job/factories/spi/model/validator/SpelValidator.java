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

import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;


public class SpelValidator implements Validator<String> {

    ExpressionParser parser = new SpelExpressionParser();

    Expression spelExpression;

    public SpelValidator(String spelExpression) {
        this.spelExpression = parser.parseExpression(spelExpression);
    }

    @Override
    public String validate(String parameterValue, ModelValidatorContext context) throws ValidationException {
        try {
            context.getSpELContext().setVariable("value", parameterValue);
            // register true / false functions
            context.getSpELContext()
                   .registerFunction("t",
                                     ModelValidatorContext.SpELVariables.class.getDeclaredMethod("t",
                                                                                                 new Class[] { Object.class }));
            context.getSpELContext()
                   .registerFunction("f",
                                     ModelValidatorContext.SpELVariables.class.getDeclaredMethod("f",
                                                                                                 new Class[] { Object.class }));
            context.getSpELContext()
                   .registerFunction("s",
                                     ModelValidatorContext.SpELVariables.class.getDeclaredMethod("s",
                                                                                                 new Class[] { Object.class }));
            Object untypedResult = spelExpression.getValue(context.getSpELContext());

            // validation can use either the 'valid' variable or the expression result as boolean
            Boolean validVariable = null;
            if (context.getSpELVariables() != null) {
                validVariable = context.getSpELVariables().getValid();
            }

            if (!(untypedResult instanceof Boolean) && (validVariable == null)) {
                throw new ValidationException("'valid' variable has not been set and SPEL expression did not return a boolean value.");
            }
            boolean evaluationResult;
            if (validVariable != null) {
                evaluationResult = validVariable;
            } else {
                evaluationResult = (Boolean) untypedResult;
            }

            if (!evaluationResult) {
                throw new ValidationException("SPEL expression returned false, received " + parameterValue);
            }
        } catch (EvaluationException e) {
            throw new ValidationException("SPEL expression raised an error: " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new ValidationException("Unexpected error: " + e.getMessage(), e);
        }
        return parameterValue;
    }
}
