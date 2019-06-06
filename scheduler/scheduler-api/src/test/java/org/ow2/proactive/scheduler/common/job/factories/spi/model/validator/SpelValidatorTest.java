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

import java.util.regex.PatternSyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobVariable;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.ow2.proactive.scheduler.common.task.JavaTask;
import org.ow2.proactive.scheduler.common.task.Task;
import org.ow2.proactive.scheduler.common.task.TaskVariable;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.collect.ImmutableMap;


public class SpelValidatorTest {

    private StandardEvaluationContext context;

    @Before
    public void before() {
        context = new StandardEvaluationContext();
        context.setTypeLocator(new ModelValidatorContext.RestrictedTypeLocator());
    }

    @Test
    public void testSpelOK() throws ValidationException {
        SpelValidator validator = new SpelValidator("#value == 'MyString'");
        String value = "MyString";
        Assert.assertEquals(value, validator.validate(value, new ModelValidatorContext(context)));
    }

    @Test(expected = ValidationException.class)
    public void testSpelKO() throws ValidationException {
        SpelValidator validator = new SpelValidator("#value == 'MyString'");
        String value = "MyString123";
        validator.validate(value, new ModelValidatorContext(context));
    }

    @Test
    public void testSpelMathOK() throws ValidationException {
        SpelValidator validator = new SpelValidator("T(java.lang.Math).random() instanceof T(Double)");
        String value = "true";
        Assert.assertEquals(value, validator.validate(value, new ModelValidatorContext(context)));
    }

    @Test
    public void testSpelXmlOK() throws ValidationException {
        SpelValidator validator = new SpelValidator("T(javax.xml.parsers.DocumentBuilderFactory).newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader('<employee id=\"101\"><name>toto</name><title>tata</title></employee>'))).getElementsByTagName('name').item(0).getTextContent() instanceof T(String)");
        String value = "toto";
        Assert.assertEquals(value, validator.validate(value, new ModelValidatorContext(context)));
    }

    @Test
    public void testSpelJSONOK() throws ValidationException {
        SpelValidator validator = new SpelValidator("new org.codehaus.jackson.map.ObjectMapper().readTree('{\"var\": \"value\"}').get('var').getTextValue() instanceof T(String)");
        String value = "value";
        Assert.assertEquals(value, validator.validate(value, new ModelValidatorContext(context)));
    }

    @Test
    public void testSpelJSONOK2() throws ValidationException {
        SpelValidator validator = new SpelValidator("new org.json.simple.parser.JSONParser().parse('{\"var\": \"value\"}').get('var').toString() instanceof T(String)");
        String value = "value";
        Assert.assertEquals(value, validator.validate(value, new ModelValidatorContext(context)));
    }

    @Test(expected = ValidationException.class)
    public void testSpelUnauthorizedType() throws ValidationException {
        SpelValidator validator = new SpelValidator("new x.y.z.Object().toString() instanceof T(String)");
        String value = "value";
        validator.validate(value, new ModelValidatorContext(context));
    }

    @Test(expected = ValidationException.class)
    public void testSpelUnauthorizedType2() throws ValidationException {
        SpelValidator validator = new SpelValidator("T(java.lang.Runtime).getRuntime().exec('hostname').waitFor() instanceof T(Integer)");
        String value = "MyString123";
        validator.validate(value, new ModelValidatorContext(context));
    }

    @Test(expected = ValidationException.class)
    public void testSpelUnauthorizedType3() throws ValidationException {
        SpelValidator validator = new SpelValidator("T(java.lang.System).getenv('HOME').waitFor() instanceof T(Integer)");
        String value = "MyString123";
        validator.validate(value, new ModelValidatorContext(context));
    }

    @Test(expected = ValidationException.class)
    public void testSpelUnauthorizedType4() throws ValidationException {
        SpelValidator validator = new SpelValidator("T(org.apache.commons.lang3.time.DateUtils).toCalendar('01/01/2000') instanceof T(Date)");
        String value = "true";
        validator.validate(value, new ModelValidatorContext(context));
    }

    @Test
    public void testSpelOKUpdateJobVariable() throws ValidationException, UserException {
        SpelValidator validator = new SpelValidator("#value == 'MyString'?(variables['var1'] = 'toto1') instanceof T(String):false");
        String value = "MyString";
        ModelValidatorContext context = new ModelValidatorContext(createJob());
        Assert.assertEquals(value, validator.validate(value, context));
        Assert.assertEquals("toto1", context.getSpELVariables().getVariables().get("var1"));
    }

    @Test
    public void testSpelKOUpdateJobVariable() throws ValidationException, UserException {
        SpelValidator validator = new SpelValidator("#value == 'MyString'?(variables['var1'] = 'toto1') instanceof T(String):false");
        String value = "MyString123";
        ModelValidatorContext context = new ModelValidatorContext(createJob());
        try {
            validator.validate(value, context);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
        Assert.assertEquals("value1", context.getSpELVariables().getVariables().get("var1"));
    }

    @Test
    public void testSpelOKUpdateJobVariableWhenEmptyOK() throws ValidationException, UserException {
        SpelValidator validator = new SpelValidator("#value == 'MyString' ? (variables['var4'] == null ? (variables['var4'] = 'toto1') instanceof T(String) : true) : false");
        String value = "MyString";
        ModelValidatorContext context = new ModelValidatorContext(createJob());
        Assert.assertEquals(value, validator.validate(value, context));
        Assert.assertEquals("toto1", context.getSpELVariables().getVariables().get("var4"));
    }

    @Test
    public void testSpelOKUpdateJobVariableWhenEmptyKO() throws ValidationException, UserException {
        SpelValidator validator = new SpelValidator("#value == 'MyString' ? (variables['var2'] == null ? (variables['var2'] = 'toto1') instanceof T(String) : true) : false");
        String value = "MyString";
        ModelValidatorContext context = new ModelValidatorContext(createJob());
        Assert.assertEquals(value, validator.validate(value, context));
        Assert.assertEquals("value2", context.getSpELVariables().getVariables().get("var2"));
    }

    @Test
    public void testSpelOKUpdateTaskVariable() throws ValidationException, UserException {
        SpelValidator validator = new SpelValidator("#value == 'MyString'?(variables['var1'] = 'toto1') instanceof T(String) : false");
        String value = "MyString";
        ModelValidatorContext context = new ModelValidatorContext(createTask());
        Assert.assertEquals(value, validator.validate(value, context));
        Assert.assertEquals("toto1", context.getSpELVariables().getVariables().get("var1"));
    }

    @Test
    public void testSpelKOUpdateTaskVariable() throws ValidationException, UserException {
        SpelValidator validator = new SpelValidator("#value == 'MyString'?(variables['var1'] = 'toto1') instanceof T(String) : false");
        String value = "MyString123";
        ModelValidatorContext context = new ModelValidatorContext(createTask());
        try {
            validator.validate(value, context);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
        Assert.assertEquals("value1", context.getSpELVariables().getVariables().get("var1"));
    }

    @Test(expected = PatternSyntaxException.class)
    @SuppressWarnings("squid:S1848")
    public void testExpressionInvalid() throws ValidationException {
        new RegexpValidator("#value == 'MyString' (");
    }

    private TaskFlowJob createJob() throws UserException {
        TaskFlowJob job = new TaskFlowJob();
        job.setVariables(ImmutableMap.of("var1",
                                         new JobVariable("var1", "value1"),
                                         "var2",
                                         new JobVariable("var2", "value2"),
                                         "var3",
                                         new JobVariable("var3", ""),
                                         "var4",
                                         new JobVariable("var4", null)));

        return job;
    }

    private Task createTask() throws UserException {

        Task task = new JavaTask();

        task.setVariables(ImmutableMap.of("var1",
                                          new TaskVariable("var1", "value1"),
                                          "var2",
                                          new TaskVariable("var2", "value2"),
                                          "var3",
                                          new TaskVariable("var3", ""),
                                          "var4",
                                          new TaskVariable("var4", null)));

        task.setName("task1");

        return task;
    }
}
