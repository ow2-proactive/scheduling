package org.ow2.proactive.perftests.rest.jmeter.sched;

import java.util.List;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.codehaus.jackson.type.TypeReference;

/**
 * Retrieves the set of known job ids from the Scheduler and stores it as a
 * JMeter property.
 */
public class RESTfulSchedJobUpdateClient extends BaseRESTfulSchedClient {

    private static final String PARAM_JOB_CLIENT_REFRESH_TIME = "jobClientRefreshTime";

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();
        defaultParameters.addArgument(PARAM_JOB_CLIENT_REFRESH_TIME,
                "${jobClientRefreshTime}");
        return defaultParameters;
    }

    @Override
    protected SampleResult doRunTest(JavaSamplerContext context)
            throws Throwable {
        setTimestamp();
        String resourceUrl = (new StringBuilder(getConnection().getUrl()))
                .append("/jobs").toString();
        SampleResult result = getResource(getClientSession(), "job ids",
                resourceUrl);
        if (result.isSuccessful()) {
            @SuppressWarnings("unchecked")
            List<String> jobIds = (List<String>) getObjectMapper().readValue(
                    result.getResponseData(),
                    new TypeReference<List<String>>() {
                    });
            if (jobIds != null && !jobIds.isEmpty()) {
                JMeterUtils.setProperty(clientSpecificJobIdKey(),
                        toString(jobIds));
            }
        }
        waitForNextCycle(getTimestamp(),
                context.getIntParameter(PARAM_JOB_CLIENT_REFRESH_TIME));
        return result;
    }

    private String toString(List<String> jobIds) {
        StringBuilder buffer = new StringBuilder();
        for (String jobId : jobIds) {
            buffer.append(jobId).append(' ');
        }
        return buffer.toString().trim();
    }

}
