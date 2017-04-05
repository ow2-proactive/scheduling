package org.ow2.proactive.wrapper;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.junit.Before;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;

/**
 * Created by root on 05/04/17.
 */
public class WrapperWebConfigurationTest {


    private WrapperWebConfiguration wrapperWebConfiguration;

    @Before
    public void setUp() throws Exception {
        wrapperWebConfiguration = new WrapperWebConfiguration();
        PASchedulerProperties.SCHEDULER_HOME.updateProperty("/src/main/resources");
        //Object content = Thread.currentThread().getContextClassLoader().getResource("/config/web/settings.ini").getContent();
    }

    @Test
    public void getRestApplicationUrl() throws Exception {

        /*String paHost = ProActiveInet.getInstance().getHostname();
        String expected = "http://"+paHost+":9080/rest";

        String actual = wrapperWebConfiguration.getRestApplicationUrl();

        assertThat(actual).isEqualTo(expected);*/
    }

}