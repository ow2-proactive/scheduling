package org.ow2.proactive.wrapper;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.junit.Test;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import javax.servlet.ServletContextEvent;

/**
 * Created by root on 05/04/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({WrapperSingleton.class, WrapperStarter.class, WrapperContextListener.class})

public class WrapperContextListenerTest extends Mockito {

    public WrapperContextListenerTest() {
    }

    @Mock
    ServletContextEvent mockEvent;

    @Mock
    private WrapperStarter mockStarter;

    @Before
    public void setup() throws Exception {
       // preparing the data by making the mock
        mockStatic(WrapperSingleton.class);
        when(WrapperSingleton.getInstance()).thenReturn(mockStarter);

        PowerMockito.mockStatic(WrapperStarter.class);
        PowerMockito.doNothing().when(WrapperStarter.class, "launchProactiveServer");
    }

    @Test
    public void testContextInitialized() throws Exception{

        new WrapperContextListener().contextInitialized(mockEvent);

        verify(mockStarter, times(1)).launchProactiveServer();

    }
}