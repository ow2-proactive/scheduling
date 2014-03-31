package org.ow2.proactive.perftests.rest.jmeter;

import static org.ow2.proactive.perftests.rest.utils.HttpUtility.STATUS_OK;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.ow2.proactive.perftests.rest.utils.HttpResponseWrapper;
import org.ow2.proactive.perftests.rest.utils.HttpUtility;
import org.ow2.proactive.tests.performance.jmeter.BaseJMeterClient;

/**
 * BaseRESTfulClient contains set of methods which are used by both
 * RESTfulRM and RESTfulScheduler clients.
 * 
 */
public abstract class BaseRESTfulClient extends BaseJMeterClient {

    private static final String SESSION_ID_SUFFIX = "-session-id";

    private long timestamp = -1;
    private ObjectMapper objectMapper;

    protected void setTimestamp() {
        timestamp = System.currentTimeMillis();
    }

    protected long getTimestamp() {
        return timestamp;
    }

    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected SampleResult getResource(String session, String resourceType,
            String resourceUrl) {
        SampleResult result = new SampleResult();
        HttpGet request = new HttpGet(resourceUrl);
        HttpResponseWrapper response = null;
        boolean success = false;
        try {
            result.sampleStart();
            response = HttpUtility.execute(session, request);
        } catch (IOException ioe) {
            logError(String.format(
                    "%s An error occurred while retrieving %s: %nUrl: %s",
                    Thread.currentThread().toString(), resourceType,
                    resourceUrl), ioe);
        } finally {
            result.sampleEnd();
        }
        if (response != null) {
            if (STATUS_OK == response.getStatusCode()) {
                if (!HttpUtility.isEmpty(response)) {
                    success = true;
                    result.setResponseData(response.getContents());                }
            } else {
                logError(String.format(
                        "%s An error occurred while retrieving %s: %n%s",
                        Thread.currentThread().toString(), resourceType,
                        new String(response.getContents())));
            }
        }
        result.setSuccessful(success);
        return result;
    }

    protected void waitForNextCycle(long startTimeInMillis,
            int refreshPeriodInSecs) {
        if (startTimeInMillis == -1) {
            return;
        }
        long remainder = (refreshPeriodInSecs * 1000)
                - (System.currentTimeMillis() - startTimeInMillis);
        if (remainder > 0) {
            try {
                Thread.sleep(remainder);
            } catch (InterruptedException ie) {
                //
            }
        }
    }

    protected String getClientSession() {
        return JMeterUtils.getProperty(getClientSessionId());
    }

    protected void setClientSession(String session) {
        JMeterUtils.setProperty(getClientSessionId(), session);
    }

    protected int getThreadNum() {
        return JMeterContextService.getContext().getThreadNum();
    }

    @Override
    protected void doSetupTest(JavaSamplerContext context) throws Throwable {
        objectMapper = new ObjectMapper().configure(
                Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private String getClientSessionId() {
        return getThreadNum() + SESSION_ID_SUFFIX;
    }
}
