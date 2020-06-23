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

import static org.mockito.Mockito.when;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.GLOBALSPACE_NAME;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.USERSPACE_NAME;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.utils.RestrictedMethodResolver;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.utils.RestrictedPropertyAccessor;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.utils.RestrictedTypeLocator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator;
import org.springframework.expression.spel.support.StandardEvaluationContext;


public class OptionalParserValidatorTest {
    private static final String VALID_DATE_FORMAT = "yyyy-M-d";

    private static final String INVALID_DATE_FORMAT = "aa-bb-cc";

    private static final String VALID_DATE = "2014-01-01";

    private static final String existUserFilePath = "my folder/exist-file.txt";

    private static final String notExistUserFilePath = "not-exist-user-file.png";

    private static final String existGlobalFilePath = "global folder/exist-file.txt";

    private static final String notExistGlobalFilePath = "not-exist-global-file.png";

    private static final String existCredential = "key";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private static File modelFile;

    private static final String validModel = "PA:LIST(a,b,c)";

    @Mock
    private ModelValidatorContext context;

    @Mock
    private SchedulerSpaceInterface schedulerSpaceInterface;

    @Mock
    private Scheduler scheduler;

    @Before
    public void init() throws SchedulerException, IOException {
        MockitoAnnotations.initMocks(this);
        when(context.getSpace()).thenReturn(schedulerSpaceInterface);
        when(schedulerSpaceInterface.checkFileExists(USERSPACE_NAME, existUserFilePath)).thenReturn(true);
        when(schedulerSpaceInterface.checkFileExists(USERSPACE_NAME, notExistUserFilePath)).thenReturn(false);
        when(schedulerSpaceInterface.checkFileExists(GLOBALSPACE_NAME, existGlobalFilePath)).thenReturn(true);
        when(schedulerSpaceInterface.checkFileExists(GLOBALSPACE_NAME, notExistGlobalFilePath)).thenReturn(false);
        when(context.getScheduler()).thenReturn(scheduler);
        when(scheduler.thirdPartyCredentialsKeySet()).thenReturn(Collections.singleton(existCredential));
        modelFile = testFolder.newFile("modelFile");
        FileUtils.writeStringToFile(modelFile, validModel, Charset.defaultCharset());
        StandardEvaluationContext spelContext = new StandardEvaluationContext();
        spelContext.setTypeLocator(new RestrictedTypeLocator());
        spelContext.setMethodResolvers(Collections.singletonList(new RestrictedMethodResolver()));
        spelContext.addPropertyAccessor(new RestrictedPropertyAccessor());
        when(context.getSpELContext()).thenReturn(spelContext);
    }

    @Test
    public void testThatValidValueIsOKForEveryModel() throws ModelSyntaxException, ValidationException,
            ConversionException, ParseException, MalformedURLException {
        testThatValidValueIsOK(ModelType.BOOLEAN, "true", true);
        testThatValidValueIsOK(ModelType.CATALOG_OBJECT,
                               "bucket_1/object10/1539310165443",
                               "bucket_1/object10/1539310165443");
        testThatValidValueIsOK(ModelType.CRON, "* * * * *", "* * * * *");
        testThatValidValueIsOK(ModelType.DATETIME,
                               "DATETIME(" + VALID_DATE_FORMAT + ")?",
                               VALID_DATE,
                               new SimpleDateFormat(VALID_DATE_FORMAT).parse(VALID_DATE));
        testThatValidValueIsOK(ModelType.DOUBLE, "10.1", 10.1D);
        testThatValidValueIsOK(ModelType.FLOAT, "3.5", 3.5F);
        testThatValidValueIsOK(ModelType.INTEGER, "7", 7);
        testThatValidValueIsOK(ModelType.JSON, " {\"name\": \"John\", \"city\":\"New York\"}");
        testThatValidValueIsOK(ModelType.LIST, "LIST(1,2,3)?", "1", "1");
        testThatValidValueIsOK(ModelType.LONG, "666", 666L);
        testThatValidValueIsOK(ModelType.MODEL_FROM_URL,
                               "MODEL_FROM_URL(" + modelFile.toURI().toURL() + ")?",
                               "a",
                               "a");
        testThatValidValueIsOK(ModelType.REGEXP, "REGEXP([a-z]+)?", "foo", "foo");
        testThatValidValueIsOK(ModelType.SHORT, "5", (short) 5);
        testThatValidValueIsOK(ModelType.SPEL, "SPEL(#value == 'MyString')?", "MyString", "MyString");
        testThatValidValueIsOK(ModelType.URI, "/my/file");
        testThatValidValueIsOK(ModelType.URL, "file://mysite");
        testThatValidHiddenValueIsOK();
        testThatValidValueIsOK(ModelType.CREDENTIAL, "key");
        testThatValidValueIsOK(ModelType.USER_FILE, existUserFilePath);
        testThatValidValueIsOK(ModelType.GLOBAL_FILE, existGlobalFilePath);
    }

    public void testThatValidValueIsOK(ModelType type, String model, String value, Object expectedParsedValue)
            throws ModelSyntaxException, ValidationException, ConversionException {
        ParserValidator<String> parserValidator = new OptionalParserValidator<>(model, type);
        Object parsedValue = parserValidator.parseAndValidate(value, context);
        Assert.assertEquals(String.format("When parsing the variable (model '%s', value '%s'), get the parsed value '%s' instead of expected '%s'",
                                          type,
                                          value,
                                          parsedValue,
                                          expectedParsedValue),
                            expectedParsedValue,
                            parsedValue);
    }

    public void testThatValidHiddenValueIsOK() throws ModelSyntaxException, ValidationException, ConversionException {
        String model = ModelType.HIDDEN.name() + ModelValidator.OPTIONAL_VARIABLE_SUFFIX;
        ParserValidator<String> parserValidator = new OptionalParserValidator<>(model, ModelType.HIDDEN);
        String parsedValue = parserValidator.parseAndValidate("pwd", context);
        Assert.assertTrue(parsedValue.startsWith("ENC("));
        Assert.assertTrue(parsedValue.endsWith(")"));
    }

    public void testThatValidValueIsOK(ModelType type, String value, Object expectedParsedValue)
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = type.name() + ModelValidator.OPTIONAL_VARIABLE_SUFFIX;
        testThatValidValueIsOK(type, model, value, expectedParsedValue);
    }

    public void testThatValidValueIsOK(ModelType type, String value)
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatValidValueIsOK(type, value, value);
    }

    @Test
    public void testThatBlankValueIsOKForEveryModel()
            throws ModelSyntaxException, ValidationException, ConversionException, MalformedURLException {
        testThatBlankValueIsOK(ModelType.BOOLEAN);
        testThatBlankValueIsOK(ModelType.CATALOG_OBJECT);
        testThatBlankValueIsOK(ModelType.CRON);
        testThatBlankValueIsOK(ModelType.DATETIME, "DATETIME(" + VALID_DATE_FORMAT + ")?");
        testThatBlankValueIsOK(ModelType.DOUBLE);
        testThatBlankValueIsOK(ModelType.FLOAT);
        testThatBlankValueIsOK(ModelType.INTEGER);
        testThatBlankValueIsOK(ModelType.JSON);
        testThatBlankValueIsOK(ModelType.LIST, "LIST(1,2,3)?");
        testThatBlankValueIsOK(ModelType.LONG);
        testThatBlankValueIsOK(ModelType.MODEL_FROM_URL, "MODEL_FROM_URL(" + modelFile.toURI().toURL() + ")?");
        testThatBlankValueIsOK(ModelType.REGEXP, "REGEXP([a-z]+)?");
        testThatBlankValueIsOK(ModelType.SHORT);
        testThatBlankValueIsOK(ModelType.SPEL, "SPEL(#value == 'MyString')?");
        testThatBlankValueIsOK(ModelType.URI);
        testThatBlankValueIsOK(ModelType.URL);
        testThatBlankValueIsOK(ModelType.HIDDEN);
        testThatBlankValueIsOK(ModelType.CREDENTIAL);
        testThatBlankValueIsOK(ModelType.USER_FILE);
        testThatBlankValueIsOK(ModelType.GLOBAL_FILE);
    }

    public void testThatBlankValueIsOK(ModelType type, String model)
            throws ModelSyntaxException, ValidationException, ConversionException {
        String value = " ";
        ParserValidator<String> parserValidator = new OptionalParserValidator<>(model, type);
        Assert.assertNull(parserValidator.parseAndValidate(value, context));
    }

    public void testThatBlankValueIsOK(ModelType type)
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = type.name() + ModelValidator.OPTIONAL_VARIABLE_SUFFIX;
        testThatBlankValueIsOK(type, model);
    }

    @Test
    public void testThatAllOptionalModelsAreCorrectlyParsed() throws ModelSyntaxException, MalformedURLException {
        testThatOptionalModelIsCorrectlyParsed(ModelType.BOOLEAN, BooleanParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.CATALOG_OBJECT, CatalogObjectParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.CRON, CRONParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.DATETIME,
                                               "DATETIME(" + VALID_DATE_FORMAT + ")?",
                                               DateTimeParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.DOUBLE, DoubleParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.FLOAT, FloatParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.INTEGER, IntegerParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.JSON, JSONParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.LIST, "LIST(1,2,3)?", ListParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.LONG, LongParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.MODEL_FROM_URL,
                                               "MODEL_FROM_URL(" + modelFile.toURI().toURL() + ")?",
                                               ModelFromURLParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.REGEXP, "REGEXP([a-z]+)?", RegexpParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.SHORT, ShortParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.SPEL,
                                               "SPEL(#value == 'MyString')?",
                                               SPELParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.URI, URIParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.URL, URLParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.HIDDEN, HiddenParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.CREDENTIAL, CredentialParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.USER_FILE, UserFileParserValidator.class);
        testThatOptionalModelIsCorrectlyParsed(ModelType.GLOBAL_FILE, GlobalFileParserValidator.class);
    }

    public void testThatOptionalModelIsCorrectlyParsed(ModelType type, String model, Class<?> expectedParentValidator)
            throws ModelSyntaxException {
        ParserValidator<?> parentValidator = new OptionalParserValidator<>(model, type).parentParserValidator;
        Assert.assertEquals(parentValidator.getClass(), expectedParentValidator);
    }

    public void testThatOptionalModelIsCorrectlyParsed(ModelType type, Class<?> expectedParentValidator)
            throws ModelSyntaxException {
        String model = type.name() + ModelValidator.OPTIONAL_VARIABLE_SUFFIX;
        testThatOptionalModelIsCorrectlyParsed(type, model, expectedParentValidator);
    }

    @Test(expected = ConversionException.class)
    public void testThatInvalidBooleanValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.BOOLEAN, "notBoolean");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidCatalogValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.CATALOG_OBJECT, "bucket_1/object10/");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidCronValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.CRON, "* * * *");
    }

    @Test(expected = ConversionException.class)
    public void testThatInvalidDatetimeValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.DATETIME, "DATETIME(" + VALID_DATE_FORMAT + ")?", "2014-06");
    }

    @Test(expected = ConversionException.class)
    public void testThatInvalidDoubleValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.DOUBLE, "aa");
    }

    @Test(expected = ConversionException.class)
    public void testThatInvalidFloatValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.FLOAT, "0b");
    }

    @Test(expected = ConversionException.class)
    public void testThatInvalidIntegerValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.INTEGER, "c");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidJsonValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.JSON, "\"test\" : 123");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidListValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.LIST, "LIST(1,2,3)?", "not-in-list-val");
    }

    @Test(expected = ConversionException.class)
    public void testThatInvalidLongValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.LONG, "l");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidModelFromUrlValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException, MalformedURLException {
        testThatInvalidValueThrowException(ModelType.MODEL_FROM_URL,
                                           "MODEL_FROM_URL(" + modelFile.toURI().toURL() + ")?",
                                           "not-in-list-val");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidRegexpValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.REGEXP, "REGEXP([a-z]+)?", "Foo");
    }

    @Test(expected = ConversionException.class)
    public void testThatInvalidShortValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.SHORT, "s");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidSpelValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.SPEL, "SPEL(#value == 'MyString')?", "AnotherString");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidUriValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.URI, "\\&¨^¨$ù%");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidUrlValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.URL, "unknown://protocol");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidCredentialValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.CREDENTIAL, "not-exist-key");
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidUserFileValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.USER_FILE, notExistUserFilePath);
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidGlobalFileValueThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        testThatInvalidValueThrowException(ModelType.GLOBAL_FILE, notExistUserFilePath);
    }

    public void testThatInvalidValueThrowException(ModelType type, String model, String invalidValue)
            throws ModelSyntaxException, ValidationException, ConversionException {
        ParserValidator<String> parserValidator = new OptionalParserValidator<>(model, type);
        parserValidator.parseAndValidate(invalidValue, context);
    }

    public void testThatInvalidValueThrowException(ModelType type, String invalidValue)
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = type.name() + ModelValidator.OPTIONAL_VARIABLE_SUFFIX;
        testThatInvalidValueThrowException(type, model, invalidValue);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidDatetimeModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = "DATETIME(" + INVALID_DATE_FORMAT + ")?";
        new OptionalParserValidator<>(model, ModelType.DATETIME).parseAndValidate("blabla");
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidDatetimeRangeModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = "DATETIME(" + VALID_DATE_FORMAT + ")[2014-05]?";
        new OptionalParserValidator<>(model, ModelType.DATETIME).parseAndValidate("blabla");
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidListModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = "LIST(1,2,3?";
        new OptionalParserValidator<>(model, ModelType.LIST).parseAndValidate("blabla");
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidSpelModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = "SPEL(#value == 'MyString' ())?";
        new OptionalParserValidator<>(model, ModelType.SPEL).parseAndValidate("blabla");
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidRegexpModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String model = "REGEXP([a-zA-Z)?";
        new OptionalParserValidator<>(model, ModelType.REGEXP).parseAndValidate("blabla");
    }
}
